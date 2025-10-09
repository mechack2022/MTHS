package com.auth.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileAttachmentDTO {
    private Long id;
    private Long videoConsultationId;
    private Long chatMessageId;
    private String originalFilename;
    private String storedFilename;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    private Long uploadedById;
    private String uploadedByType;
    private String uploadedByName;
    private Boolean isMedicalRecord;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper fields for UI
    private String fileSizeFormatted;
    private Boolean isImage;
    private Boolean isDocument;
    private String uploadTimeFormatted;
    
    // Download/view URLs
    private String downloadUrl;
    private String previewUrl;
}