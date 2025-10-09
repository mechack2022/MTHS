package com.auth.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "file_attachments")
@Data
@EqualsAndHashCode(callSuper = false)
public class FileAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_consultation_id", nullable = false)
    private VideoConsultation videoConsultation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "uploaded_by_id", nullable = false)
    private Long uploadedById;

    @Column(name = "uploaded_by_type", nullable = false)
    private String uploadedByType; // PATIENT, DOCTOR

    @Column(name = "is_medical_record")
    private Boolean isMedicalRecord = false;

    @Column(name = "description")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    // Helper methods
    public Long getVideoConsultationId() {
        return videoConsultation != null ? videoConsultation.getId() : null;
    }

    public Long getChatMessageId() {
        return chatMessage != null ? chatMessage.getId() : null;
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isDocument() {
        return mimeType != null && (
            mimeType.equals("application/pdf") ||
            mimeType.startsWith("application/msword") ||
            mimeType.startsWith("application/vnd.openxmlformats-officedocument")
        );
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 KB";
        
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return (fileSize / 1024) + " KB";
        if (fileSize < 1024 * 1024 * 1024) return (fileSize / (1024 * 1024)) + " MB";
        return (fileSize / (1024 * 1024 * 1024)) + " GB";
    }
}