# Video Consultation Redis Implementation - Testing Guide

## Prerequisites

1. **Redis Running**:
   ```bash
   docker-compose up -d redis
   docker exec healthcare_redis redis-cli ping  # Should return PONG
   ```

2. **Spring Boot Application Running**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Required Data**:
   - At least one User (Patient)
   - At least one User (Doctor)
   - A valid Appointment with both patient and doctor
   - Authentication token (JWT)

## Testing Flow Overview

```
1. Create/Start Video Consultation → Redis session initialized
2. Join Consultation (Patient)      → Join event recorded in Redis
3. Join Consultation (Doctor)       → Join event recorded in Redis
4. WebSocket Connection             → Real-time events to Redis
5. Send Chat Messages               → Chat recorded in Redis
6. Media State Changes              → Media states tracked in Redis
7. Screen Sharing                   → Screen events in Redis
8. End Consultation                 → Flush Redis data to PostgreSQL
9. Verify Data                      → Check PostgreSQL for persisted data
```

---

## Test Scenarios

### Scenario 1: Complete Consultation Flow

#### Step 1: Start Video Consultation

**Endpoint**: `POST /api/v1/video-consultations/appointments/{appointmentId}/start`

**Request**:
```bash
curl -X POST http://localhost:8081/api/v1/video-consultations/appointments/1/start \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "recordingEnabled": true,
    "consultationSettings": {
      "videoQuality": "HD",
      "audioEnabled": true
    }
  }'
```

**Expected Response**:
```json
{
  "status": "success",
  "message": "Video consultation started successfully",
  "data": {
    "id": 1,
    "sessionId": "session_abc123...",
    "roomId": "room_xyz789...",
    "status": "SCHEDULED",
    "recordingEnabled": true,
    "appointmentId": 1
  },
  "statusCode": 200
}
```

**✅ Verify in Redis**:
```bash
# Check if session was created
docker exec healthcare_redis redis-cli KEYS "consultation:session:*"

# View session data
docker exec healthcare_redis redis-cli GET "consultation:session:session_abc123..."
```

**Expected in Redis**: A new session entry with status "SCHEDULED"

---

#### Step 2: Patient Joins Consultation

**Endpoint**: `POST /api/v1/video-consultations/{consultationId}/join`

**Request**:
```bash
curl -X POST http://localhost:8081/api/v1/video-consultations/1/join \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer PATIENT_JWT_TOKEN" \
  -d '{
    "userId": 10,
    "userType": "PATIENT",
    "deviceInfo": {
      "browser": "Chrome",
      "os": "MacOS"
    }
  }'
```

**Expected Response**:
```json
{
  "status": "success",
  "message": "Successfully joined consultation",
  "data": {
    "sessionId": "session_abc123...",
    "roomId": "room_xyz789...",
    "token": "token_session_abc123..._10_PATIENT",
    "iceServers": {...},
    "mediaConstraints": {...},
    "recordingEnabled": true,
    "participants": [...]
  }
}
```

**✅ Verify in Application Logs**:
```
INFO  - User 10 (PATIENT) joined video consultation 1
INFO  - Recorded participant join - Session: session_abc123, User: 10, Type: PATIENT
```

---

#### Step 3: Doctor Joins Consultation

**Request**:
```bash
curl -X POST http://localhost:8081/api/v1/video-consultations/1/join \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DOCTOR_JWT_TOKEN" \
  -d '{
    "userId": 5,
    "userType": "DOCTOR",
    "deviceInfo": {
      "browser": "Firefox",
      "os": "Windows"
    }
  }'
```

**✅ Verify in Redis**:
```bash
# Get session data
docker exec healthcare_redis redis-cli GET "consultation:session:session_abc123..."
```

**Expected**: Session should show:
- `patientJoinedAt`: timestamp
- `doctorJoinedAt`: timestamp
- `patientCurrentlyConnected`: true
- `doctorCurrentlyConnected`: true
- `status`: "IN_PROGRESS"

---

#### Step 4: Connect via WebSocket

**WebSocket URL**: `ws://localhost:8081/ws/video-consultation?userId=10&consultationId=session_abc123&userType=PATIENT`

**Test with JavaScript** (Browser Console):
```javascript
const ws = new WebSocket('ws://localhost:8081/ws/video-consultation?userId=10&consultationId=session_abc123&userType=PATIENT');

ws.onopen = () => {
  console.log('WebSocket connected');
};

ws.onmessage = (event) => {
  console.log('Received:', JSON.parse(event.data));
};

// Send chat message
ws.send(JSON.stringify({
  type: 'CHAT_MESSAGE',
  message: 'Hello Doctor!',
  timestamp: Date.now()
}));

// Toggle video
ws.send(JSON.stringify({
  type: 'MEDIA_STATE_CHANGE',
  videoEnabled: false,
  audioEnabled: true
}));

// Start screen share
ws.send(JSON.stringify({
  type: 'SCREEN_SHARE_START'
}));
```

**✅ Verify in Application Logs**:
```
INFO  - User 10 joined video consultation session_abc123 via WebSocket
DEBUG - Recorded chat message - Session: session_abc123, Sender: 10
DEBUG - Recorded media state change - Session: session_abc123, User: 10
INFO  - Recorded screen share STARTED - Session: session_abc123, User: 10
```

---

#### Step 5: End Consultation

**Endpoint**: `PUT /api/v1/video-consultations/{consultationId}/end`

**Request**:
```bash
curl -X PUT http://localhost:8081/api/v1/video-consultations/1/end \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DOCTOR_JWT_TOKEN" \
  -d '{
    "consultationNotes": "Patient showed good progress",
    "diagnosis": "Improving condition",
    "rating": 5
  }'
```

**Expected Response**:
```json
{
  "status": "success",
  "message": "Consultation ended successfully",
  "data": {
    "id": 1,
    "status": "COMPLETED",
    "endTime": "2026-03-17T12:30:00",
    "durationMinutes": 25,
    ...
  }
}
```

**✅ Verify in Application Logs**:
```
INFO  - Session marked as completed: session_abc123
INFO  - Flushing session to PostgreSQL: session_abc123
INFO  - Flushed 5 chat messages to PostgreSQL
INFO  - Successfully flushed session session_abc123 to PostgreSQL
```

**✅ Verify in Redis**:
```bash
# Check session is marked as flushed
docker exec healthcare_redis redis-cli GET "consultation:session:session_abc123..."
```

**Expected**: `flushedToDatabase: true`, `flushedAt: timestamp`

**✅ Verify in PostgreSQL**:
```sql
-- Check video_consultations table
SELECT id, status, patient_joined_at, doctor_joined_at,
       end_time, duration_minutes
FROM video_consultations
WHERE id = 1;

-- Check chat_messages table
SELECT id, sender_id, sender_type, content, sent_at
FROM chat_messages
WHERE video_consultation_id = 1;
```

---

## Test Scenario 2: Scheduled Flush (Background Task)

This tests the automatic flush that runs every 5 minutes.

1. **Create and Complete a Consultation** (follow steps above)
2. **Don't manually end it** - let the scheduled task handle it
3. **Wait 5 minutes**
4. **Check logs**:
   ```
   INFO  - Starting scheduled flush of completed consultation sessions
   INFO  - Found 1 unflushed sessions
   INFO  - Flushing session to PostgreSQL: session_abc123
   INFO  - Flushed 1 sessions to PostgreSQL
   INFO  - Completed scheduled flush of consultation sessions
   ```

---

## Test Scenario 3: Connection Quality Tracking

**Endpoint**: `POST /api/v1/video-consultations/{consultationId}/connection-quality`

**Request**:
```bash
curl -X POST http://localhost:8081/api/v1/video-consultations/1/connection-quality \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer PATIENT_JWT_TOKEN" \
  -d '{
    "userId": 10,
    "userType": "PATIENT",
    "packetLoss": 2,
    "jitter": 10,
    "roundTripTime": 50,
    "bandwidth": 1500,
    "qualityRating": "GOOD"
  }'
```

**✅ Verify**: Quality metrics should be stored in Redis session

---

## Redis Data Verification Commands

### View All Sessions
```bash
docker exec healthcare_redis redis-cli KEYS "consultation:session:*"
```

### View Specific Session
```bash
docker exec healthcare_redis redis-cli GET "consultation:session:session_abc123"
```

### View Session TTL (Time To Live)
```bash
docker exec healthcare_redis redis-cli TTL "consultation:session:session_abc123"
# Returns remaining seconds (86400 = 24 hours)
```

### Monitor Redis Commands in Real-Time
```bash
docker exec healthcare_redis redis-cli MONITOR
```

### View Redis Memory Usage
```bash
docker exec healthcare_redis redis-cli INFO memory
```

---

## PostgreSQL Verification Queries

```sql
-- Check video consultations
SELECT * FROM video_consultations WHERE id = 1;

-- Check chat messages
SELECT * FROM chat_messages WHERE video_consultation_id = 1;

-- Check timestamps
SELECT
    id,
    patient_joined_at,
    patient_left_at,
    doctor_joined_at,
    doctor_left_at,
    start_time,
    end_time,
    duration_minutes
FROM video_consultations
WHERE id = 1;
```

---

## Common Testing Issues & Solutions

### Issue 1: Redis Connection Failed
```
JedisConnectionException: Failed to connect to any host
```

**Solution**:
```bash
# Check Redis is running
docker ps | grep redis

# Start Redis
docker-compose up -d redis

# Test connection
docker exec healthcare_redis redis-cli ping
```

### Issue 2: Session Not Found in Redis
**Possible Causes**:
- Session expired (24-hour TTL)
- Session was already flushed and deleted
- Redis was restarted

**Solution**: Create a new consultation

### Issue 3: Data Not Flushing to PostgreSQL
**Check**:
1. Application logs for errors
2. PostgreSQL connection
3. Transaction rollbacks

### Issue 4: WebSocket Connection Failed
**Check**:
1. WebSocket endpoint configuration
2. User authentication
3. Session ID format

---

## Performance Testing

### Load Test: Multiple Consultations
```bash
# Create 10 concurrent consultations
for i in {1..10}; do
  curl -X POST http://localhost:8081/api/v1/video-consultations/appointments/$i/start \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer YOUR_TOKEN" \
    -d '{"recordingEnabled": false}' &
done
wait

# Check Redis
docker exec healthcare_redis redis-cli KEYS "consultation:session:*" | wc -l
# Should show 10 sessions
```

### Memory Usage Test
```bash
# Before consultations
docker exec healthcare_redis redis-cli INFO memory | grep used_memory_human

# After 100 consultations
# Check memory growth

# After flush
# Memory should decrease
```

---

## Integration Test Checklist

✅ **Session Lifecycle**:
- [ ] Session created in Redis on consultation start
- [ ] Session updated on participant join
- [ ] Session records chat messages
- [ ] Session tracks media state changes
- [ ] Session records screen sharing
- [ ] Session marked complete on end
- [ ] Session flushed to PostgreSQL
- [ ] Session data matches in both Redis and PostgreSQL

✅ **WebSocket**:
- [ ] Connection established successfully
- [ ] Messages sent and received
- [ ] Events recorded in Redis
- [ ] Broadcast to all participants works

✅ **Scheduled Tasks**:
- [ ] Scheduler runs every 5 minutes
- [ ] Completed sessions flushed automatically
- [ ] No errors in logs

✅ **Data Persistence**:
- [ ] VideoConsultation entity updated
- [ ] ChatMessage entities created
- [ ] Timestamps accurate
- [ ] Duration calculated correctly

---

## Postman Collection

Import this collection for easier testing:

```json
{
  "info": {
    "name": "Video Consultation API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Start Consultation",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/v1/video-consultations/appointments/{{appointmentId}}/start",
        "header": [
          {"key": "Authorization", "value": "Bearer {{jwt_token}}"},
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"recordingEnabled\": true}"
        }
      }
    },
    {
      "name": "Join Consultation",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/v1/video-consultations/{{consultationId}}/join",
        "header": [
          {"key": "Authorization", "value": "Bearer {{jwt_token}}"},
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"userId\": {{userId}}, \"userType\": \"PATIENT\", \"deviceInfo\": {}}"
        }
      }
    },
    {
      "name": "End Consultation",
      "request": {
        "method": "PUT",
        "url": "{{base_url}}/api/v1/video-consultations/{{consultationId}}/end",
        "header": [
          {"key": "Authorization", "value": "Bearer {{jwt_token}}"},
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"consultationNotes\": \"Test completed\", \"diagnosis\": \"Normal\", \"rating\": 5}"
        }
      }
    }
  ],
  "variable": [
    {"key": "base_url", "value": "http://localhost:8081"},
    {"key": "appointmentId", "value": "1"},
    {"key": "consultationId", "value": "1"},
    {"key": "userId", "value": "10"},
    {"key": "jwt_token", "value": "YOUR_TOKEN_HERE"}
  ]
}
```

---

## Summary

The complete test flow:
1. **Start** → Creates session in Redis
2. **Join** → Records participant events
3. **WebSocket** → Real-time event tracking
4. **End** → Flushes to PostgreSQL
5. **Verify** → Check both Redis and PostgreSQL

Redis provides fast writes during active calls, PostgreSQL stores permanent records!
