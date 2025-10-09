package com.auth.service.repository;

import com.auth.service.entity.ChatMessage;
import com.auth.service.entity.FileAttachment;
import com.auth.service.entity.VideoConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    
    List<FileAttachment> findByVideoConsultation(VideoConsultation videoConsultation);
    
    List<FileAttachment> findByChatMessage(ChatMessage chatMessage);
    
    List<FileAttachment> findByUploadedById(Long uploadedById);
    
    List<FileAttachment> findByUploadedByIdAndUploadedByType(Long uploadedById, String uploadedByType);
    
    List<FileAttachment> findByFileType(String fileType);
    
    List<FileAttachment> findByIsMedicalRecord(Boolean isMedicalRecord);
    
    @Query("SELECT fa FROM FileAttachment fa WHERE fa.videoConsultation = :consultation AND fa.fileType = :fileType ORDER BY fa.createdAt DESC")
    List<FileAttachment> findByConsultationAndFileType(@Param("consultation") VideoConsultation consultation, @Param("fileType") String fileType);
    
    @Query("SELECT fa FROM FileAttachment fa WHERE fa.videoConsultation = :consultation AND fa.isMedicalRecord = true ORDER BY fa.createdAt DESC")
    List<FileAttachment> findMedicalRecordsByConsultation(@Param("consultation") VideoConsultation consultation);
    
    @Query("SELECT fa FROM FileAttachment fa WHERE fa.uploadedById = :userId AND fa.createdAt BETWEEN :startDate AND :endDate ORDER BY fa.createdAt DESC")
    List<FileAttachment> findByUploaderAndDateRange(@Param("userId") Long userId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(fa) FROM FileAttachment fa WHERE fa.videoConsultation = :consultation")
    long countAttachmentsByConsultation(@Param("consultation") VideoConsultation consultation);
    
    @Query("SELECT SUM(fa.fileSize) FROM FileAttachment fa WHERE fa.videoConsultation = :consultation")
    Long getTotalFileSizeByConsultation(@Param("consultation") VideoConsultation consultation);
    
    @Query("SELECT SUM(fa.fileSize) FROM FileAttachment fa WHERE fa.uploadedById = :userId")
    Long getTotalFileSizeByUploader(@Param("userId") Long userId);
    
    @Query("SELECT fa FROM FileAttachment fa WHERE fa.originalFilename LIKE %:filename% ORDER BY fa.createdAt DESC")
    List<FileAttachment> findByOriginalFilenameContaining(@Param("filename") String filename);
    
    @Query("SELECT fa FROM FileAttachment fa WHERE fa.description LIKE %:description% ORDER BY fa.createdAt DESC")
    List<FileAttachment> findByDescriptionContaining(@Param("description") String description);
    
    @Query("SELECT DISTINCT fa.fileType FROM FileAttachment fa WHERE fa.videoConsultation = :consultation")
    List<String> findDistinctFileTypesByConsultation(@Param("consultation") VideoConsultation consultation);
    
    @Query("SELECT fa FROM FileAttachment fa WHERE fa.fileSize > :minSize ORDER BY fa.fileSize DESC")
    List<FileAttachment> findLargeFiles(@Param("minSize") Long minSize);
    
    @Query("SELECT fa FROM FileAttachment fa WHERE fa.createdAt >= :fromDate ORDER BY fa.createdAt DESC")
    List<FileAttachment> findRecentAttachments(@Param("fromDate") LocalDateTime fromDate);
    
    void deleteByVideoConsultation(VideoConsultation videoConsultation);
    
    void deleteByChatMessage(ChatMessage chatMessage);
}