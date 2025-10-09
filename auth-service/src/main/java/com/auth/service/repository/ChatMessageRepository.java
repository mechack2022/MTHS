package com.auth.service.repository;

import com.auth.service.entity.ChatMessage;
import com.auth.service.entity.User;
import com.auth.service.entity.VideoConsultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByVideoConsultation(VideoConsultation videoConsultation, Pageable pageable);
    
    List<ChatMessage> findByVideoConsultationOrderBySentAtAsc(VideoConsultation videoConsultation);
    
    List<ChatMessage> findByVideoConsultationOrderBySentAtDesc(VideoConsultation videoConsultation);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.videoConsultation = :consultation AND cm.senderId != :senderId AND cm.readAt IS NULL AND cm.deleted = false")
    List<ChatMessage> findUnreadMessagesByConsultationAndNotSender(@Param("consultation") VideoConsultation consultation, @Param("senderId") Long senderId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.videoConsultation = :consultation AND cm.senderId != :senderId AND cm.readAt IS NULL AND cm.deleted = false")
    int countUnreadMessagesByConsultationAndNotSender(@Param("consultation") VideoConsultation consultation, @Param("senderId") Long senderId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.videoConsultation = :consultation AND LOWER(cm.content) LIKE LOWER(CONCAT('%', :searchQuery, '%')) AND cm.deleted = false ORDER BY cm.sentAt DESC")
    List<ChatMessage> searchMessagesByContent(@Param("consultation") VideoConsultation consultation, @Param("searchQuery") String searchQuery);
    
    List<ChatMessage> findBySenderId(Long senderId);
    
    List<ChatMessage> findByMessageType(String messageType);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.videoConsultation = :consultation AND cm.sentAt BETWEEN :startDate AND :endDate AND cm.deleted = false ORDER BY cm.sentAt ASC")
    List<ChatMessage> findMessagesByConsultationAndDateRange(@Param("consultation") VideoConsultation consultation, 
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.videoConsultation = :consultation AND cm.status = :status AND cm.deleted = false")
    List<ChatMessage> findByConsultationAndStatus(@Param("consultation") VideoConsultation consultation, @Param("status") com.auth.service.constants.MessageStatus status);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.videoConsultation = :consultation AND cm.deleted = false")
    long countMessagesByConsultation(@Param("consultation") VideoConsultation consultation);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.senderId = :senderId AND cm.sentAt >= :fromDate")
    long countMessagesBySenderSince(@Param("senderId") Long senderId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.videoConsultation = :consultation AND cm.messageType = 'FILE' AND cm.deleted = false ORDER BY cm.sentAt DESC")
    List<ChatMessage> findAttachmentMessagesByConsultation(@Param("consultation") VideoConsultation consultation);
    
    @Query("SELECT DISTINCT cm.senderType FROM ChatMessage cm WHERE cm.videoConsultation = :consultation")
    List<String> findDistinctSenderTypesByConsultation(@Param("consultation") VideoConsultation consultation);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.videoConsultation.id IN :consultationIds AND cm.deleted = false ORDER BY cm.sentAt DESC")
    List<ChatMessage> findRecentMessagesByConsultations(@Param("consultationIds") List<Long> consultationIds, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.readAt IS NULL AND cm.senderId != :userId AND (cm.videoConsultation.appointment.patientProfile.user.id = :userId OR cm.videoConsultation.appointment.doctorProfile.user.id = :userId)")
    List<ChatMessage> findUnreadMessagesForUser(@Param("userId") Long userId);
    
    void deleteByVideoConsultation(VideoConsultation videoConsultation);

    
    Page<ChatMessage> findByVideoConsultationAndIdLessThanOrderByCreatedAtDesc(VideoConsultation consultation, Long messageId, Pageable pageable);
}
