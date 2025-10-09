package com.auth.service.mapper;

import com.auth.service.dto.FileAttachmentDTO;
import com.auth.service.entity.FileAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.DecimalFormat;

@Mapper(componentModel = "spring")
public interface FileAttachmentMapper {

    @Mapping(target = "fileSizeFormatted", source = "fileSize", qualifiedByName = "formatFileSize")
    @Mapping(target = "isImage", source = "fileType", qualifiedByName = "isImageType")
    @Mapping(target = "isDocument", source = "fileType", qualifiedByName = "isDocumentType")
    @Mapping(target = "uploadTimeFormatted", source = "createdAt", qualifiedByName = "formatUploadTime")
    @Mapping(target = "downloadUrl", source = "fileUrl")
    @Mapping(target = "previewUrl", source = "fileUrl", qualifiedByName = "generatePreviewUrl")
    @Mapping(target = "uploadedByName", ignore = true) // Will be set manually in service
    FileAttachmentDTO toDTO(FileAttachment fileAttachment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "videoConsultation", ignore = true)
    @Mapping(target = "chatMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    FileAttachment toEntity(FileAttachmentDTO dto);

    @Named("formatFileSize")
    default String formatFileSize(Long fileSize) {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(size) + " " + units[unitIndex];
    }

    @Named("isImageType")
    default Boolean isImageType(String fileType) {
        return "IMAGE".equals(fileType);
    }

    @Named("isDocumentType")
    default Boolean isDocumentType(String fileType) {
        return "DOCUMENT".equals(fileType) || "PDF".equals(fileType);
    }

    @Named("formatUploadTime")
    default String formatUploadTime(java.time.LocalDateTime uploadTime) {
        if (uploadTime == null) {
            return null;
        }
        
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        return uploadTime.format(formatter);
    }

    @Named("generatePreviewUrl")
    default String generatePreviewUrl(String fileUrl) {
        // In a real implementation, you might generate thumbnail URLs for images
        // or preview URLs for documents. For now, return the same URL.
        return fileUrl;
    }
}