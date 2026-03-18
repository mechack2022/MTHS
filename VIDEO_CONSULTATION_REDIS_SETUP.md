# Video Consultation Redis Persistence Implementation

## Overview

This implementation adds a two-tier caching strategy for video consultation persistence:
- **During call** → Redis (fast, temporary storage for real-time events)
- **Call ends** → Flush to PostgreSQL (permanent storage)

## Architecture

```
┌─────────────────┐
│  WebSocket      │
│  Connection     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  VideoConsultationWebSocketHandler  │
│  - Handles real-time events         │
│  - Records to Redis via CacheService│
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  VideoConsultationCacheService      │
│  - Manages Redis operations         │
│  - Flushes to PostgreSQL on end     │
└────────┬────────────────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌──────┐  ┌──────────┐
│Redis │  │PostgreSQL│
│(Temp)│  │(Permanent)│
└──────┘  └──────────┘
```

## Components Created

### 1. Redis Configuration
**File**: `backend/src/main/java/com/mths/shared/config/RedisConfig.java`
- Configures Redis connection using Jedis
- Sets up JSON serialization with Jackson
- Configures ObjectMapper for LocalDateTime support

### 2. Redis Entity
**File**: `backend/src/main/java/com/mths/consultation/entity/VideoConsultationSession.java`
- Stores real-time session data in Redis
- TTL: 24 hours (auto-expires if not flushed)
- Tracks:
  - Participant join/leave events
  - Chat messages
  - Media state changes (mute/unmute)
  - Connection quality metrics
  - Screen sharing events
  - WebRTC signaling events

### 3. Redis Repository
**File**: `backend/src/main/java/com/mths/consultation/repository/VideoConsultationSessionRepository.java`
- Spring Data Redis repository
- Query methods for finding sessions by ID, status, patient, doctor

### 4. Cache Service
**File**: `backend/src/main/java/com/mths/consultation/service/VideoConsultationCacheService.java`
- **Key Methods**:
  - `initializeSession()` - Creates Redis session when consultation starts
  - `recordParticipantJoin()` - Records when participant joins
  - `recordParticipantLeave()` - Records when participant leaves
  - `recordChatMessage()` - Stores chat messages in Redis
  - `recordMediaStateChange()` - Tracks video/audio mute/unmute
  - `recordConnectionQuality()` - Stores connection quality metrics
  - `recordScreenShare()` - Tracks screen sharing events
  - `completeSession()` - Marks session as completed
  - `flushSessionToDatabase()` - Transfers data from Redis to PostgreSQL
  - `flushAllCompletedSessions()` - Batch flush (used by scheduler)

### 5. Scheduler
**File**: `backend/src/main/java/com/mths/consultation/scheduler/VideoConsultationScheduler.java`
- Runs every 5 minutes to flush completed sessions
- Ensures data is persisted even if manual flush fails

### 6. Updated Files

#### WebSocket Handler
**File**: `backend/src/main/java/com/mths/shared/websocket/VideoConsultationWebSocketHandler.java`
- Integrated with `VideoConsultationCacheService`
- Records all events to Redis:
  - Participant join/leave
  - Chat messages
  - Media state changes
  - Screen sharing events

#### Video Consultation Service
**File**: `backend/src/main/java/com/mths/consultation/service/VideoConsultationServiceImpl.java`
- Initializes Redis session when consultation is created
- Flushes to PostgreSQL when consultation ends

#### Main Application
**File**: `backend/src/main/java/com/mths/MTHSApplication.java`
- Added `@EnableScheduling` annotation

## Configuration

### Environment Variables

Add to your `.env` file:

```env
# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_TIMEOUT=60000
```

### Application Configuration

Updated `application.yml` with Redis settings:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: ${REDIS_TIMEOUT:60000}
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
      repositories:
        enabled: true
```

## Data Flow

### 1. Consultation Creation
```java
VideoConsultation consultation = createVideoConsultation(request);
// → Saves to PostgreSQL
cacheService.initializeSession(consultation);
// → Creates Redis session
```

### 2. During Active Call
```java
// All WebSocket events automatically recorded to Redis
webSocketHandler.handleMessage(session, message);
// ↓
cacheService.recordChatMessage(...);
cacheService.recordMediaStateChange(...);
cacheService.recordScreenShare(...);
// → Fast writes to Redis
```

### 3. Call Ends
```java
endConsultation(consultationId);
// ↓
cacheService.completeSession(sessionId);
cacheService.flushSessionToDatabase(sessionId);
// → Transfers all data from Redis to PostgreSQL
// → Updates VideoConsultation entity
// → Saves chat messages to ChatMessage table
```

### 4. Scheduled Backup
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
flushCompletedSessions();
// → Flushes any completed sessions that weren't manually flushed
```

## What Gets Stored in Redis

### VideoConsultationSession Structure
```json
{
  "sessionId": "session_abc123",
  "consultationId": 1,
  "roomId": "room_xyz789",
  "status": "IN_PROGRESS",
  "events": [
    {
      "eventType": "PARTICIPANT_JOINED",
      "userId": 123,
      "userType": "PATIENT",
      "timestamp": "2026-03-16T10:30:00"
    }
  ],
  "chatMessages": [
    {
      "messageId": "msg_123",
      "senderId": 123,
      "senderType": "PATIENT",
      "content": "Hello doctor",
      "messageType": "TEXT",
      "sentAt": "2026-03-16T10:31:00"
    }
  ],
  "participantMediaStates": {
    "123_PATIENT": {
      "userId": 123,
      "videoEnabled": true,
      "audioEnabled": false,
      "lastUpdated": "2026-03-16T10:32:00"
    }
  },
  "qualityMetrics": [...],
  "screenShareEvents": [...],
  "signalingEvents": [...]
}
```

## What Gets Flushed to PostgreSQL

### VideoConsultation Table
- `patient_joined_at`
- `patient_left_at`
- `doctor_joined_at`
- `doctor_left_at`
- `end_time`
- `duration_minutes`
- `status`

### ChatMessage Table
- All chat messages from Redis session
- Converted to proper entity format
- Associated with VideoConsultation

## Redis Key Pattern

```
consultation:session:{sessionId}
```

Example: `consultation:session:session_a1b2c3d4e5f6`

## TTL (Time To Live)

- **Session TTL**: 24 hours
- Sessions auto-expire after 24 hours if not deleted
- Flushed sessions should be manually deleted after successful flush (optional)

## Installation Steps

### 1. Install Redis (if not already installed)

**macOS:**
```bash
brew install redis
brew services start redis
```

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install redis-server
sudo systemctl start redis-server
```

**Docker:**
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

### 2. Verify Redis Connection
```bash
redis-cli ping
# Should return: PONG
```

### 3. Update Dependencies
```bash
cd backend
./mvnw clean install
```

### 4. Run Application
```bash
./mvnw spring-boot:run
```

## Testing

### 1. Check Redis Connection
After starting the application, check logs for:
```
Connected to Redis at localhost:6379
```

### 2. Start a Consultation
```bash
curl -X POST http://localhost:8081/api/consultations \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "scheduledStartTime": "2026-03-16T10:00:00",
    "estimatedDuration": 30
  }'
```

### 3. Check Redis
```bash
redis-cli
> KEYS consultation:session:*
> GET consultation:session:session_abc123
```

### 4. End Consultation
```bash
curl -X POST http://localhost:8081/api/consultations/1/end
```

### 5. Verify PostgreSQL
Check that data was flushed to `video_consultations` and `chat_messages` tables.

## Monitoring

### Redis CLI Commands
```bash
# List all sessions
redis-cli KEYS "consultation:session:*"

# Get session data
redis-cli GET "consultation:session:session_abc123"

# Check TTL
redis-cli TTL "consultation:session:session_abc123"

# Monitor real-time commands
redis-cli MONITOR
```

### Application Logs
```
INFO  - Initializing Redis session for consultation: 1
INFO  - Recorded participant join - Session: session_abc123, User: 123, Type: PATIENT
INFO  - Recorded chat message - Session: session_abc123, Sender: 123
INFO  - Session marked as completed: session_abc123
INFO  - Flushing session to PostgreSQL: session_abc123
INFO  - Flushed 5 chat messages to PostgreSQL
INFO  - Successfully flushed session session_abc123 to PostgreSQL
```

## Performance Benefits

1. **Fast Writes**: Redis in-memory storage = ~100x faster than PostgreSQL for writes
2. **Reduced DB Load**: Database writes happen once (at end) instead of continuously
3. **Real-time Access**: Session data instantly available for analytics/monitoring
4. **Automatic Cleanup**: TTL ensures old sessions don't consume memory forever
5. **Fault Tolerance**: Scheduled flush ensures data isn't lost even if manual flush fails

## Troubleshooting

### Redis Connection Errors
```
Error: Could not connect to Redis at localhost:6379
```
**Solution**: Ensure Redis is running
```bash
redis-cli ping
# or
brew services restart redis
```

### Session Not Found
```
Cannot flush session - not found in Redis
```
**Solution**: Session may have expired (24hr TTL) or was already deleted

### Flush Failures
- Check PostgreSQL connection
- Verify `VideoConsultation` entity exists
- Check logs for specific error messages

## Future Enhancements

1. **Redis Cluster**: For high availability and horizontal scaling
2. **Compression**: Compress large session data before storing
3. **Analytics**: Real-time analytics on active consultations
4. **Replay**: Ability to replay consultation events for debugging
5. **Metrics**: Expose Redis metrics via Spring Actuator

## Summary

You now have a complete Redis-based caching solution for video consultations that:
- ✅ Stores real-time events in Redis during calls
- ✅ Automatically flushes to PostgreSQL when calls end
- ✅ Has scheduled backup flush every 5 minutes
- ✅ Auto-expires old sessions after 24 hours
- ✅ Tracks all consultation events (chat, media, quality, etc.)
- ✅ Integrates seamlessly with existing WebSocket infrastructure

All real-time events are now captured in Redis for fast access and eventually persisted to PostgreSQL for permanent storage!
