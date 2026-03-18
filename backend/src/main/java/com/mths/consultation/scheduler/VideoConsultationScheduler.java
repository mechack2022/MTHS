package com.mths.consultation.scheduler;

import com.mths.consultation.service.VideoConsultationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for video consultation management
 * Handles periodic flushing of completed Redis sessions to PostgreSQL
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VideoConsultationScheduler {

    private final VideoConsultationCacheService cacheService;

    /**
     * Flush completed consultation sessions from Redis to PostgreSQL
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void flushCompletedSessions() {
        log.info("Starting scheduled flush of completed consultation sessions");
        try {
            cacheService.flushAllCompletedSessions();
            log.info("Completed scheduled flush of consultation sessions");
        } catch (Exception e) {
            log.error("Error during scheduled flush of consultation sessions", e);
        }
    }

    /**
     * Cleanup old flushed sessions from Redis
     * Runs every hour to prevent Redis memory bloat
     * This can be customized based on your requirements
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupOldSessions() {
        log.info("Scheduled cleanup of old sessions is handled by Redis TTL (24 hours)");
        // Redis TTL automatically handles cleanup after 24 hours
        // This method is a placeholder for any additional cleanup logic
    }
}
