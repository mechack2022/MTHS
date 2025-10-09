package com.auth.service.service;

import com.auth.service.config.MinIOProperties;
import com.auth.service.constants.FileCategory;
import com.auth.service.dto.FileUploadResponse;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class FileUploadService {

    private final MinioClient minioClient;
    private final MinIOProperties minioBucketsConfig;

    @PostConstruct
    public void init() {
        try {
            if (minioBucketsConfig.getBuckets() == null || minioBucketsConfig.getBuckets().isEmpty()) {
                log.warn("No MinIO buckets configured in application.yml. Skipping bucket initialization.");
                return;
            }
            
            if (minioBucketsConfig.getEndpoint() == null || minioBucketsConfig.getAccessKey() == null) {
                log.warn("MinIO configuration incomplete. Endpoint: {}, AccessKey: {}. Skipping bucket initialization.", 
                    minioBucketsConfig.getEndpoint(), minioBucketsConfig.getAccessKey() != null ? "SET" : "NULL");
                return;
            }
            
            minioBucketsConfig.getBuckets().values().forEach(this::ensureBucketExists);
        } catch (Exception e) {
            log.error("Failed to initialize MinIO buckets. Service will continue without file upload capability: {}", e.getMessage());
        }
    }

    public FileUploadResponse uploadFile(MultipartFile file, FileCategory category) {
        validateFile(file, category);

        String bucketName = resolveBucketName(category);
        String folder = category.name().toLowerCase().replace("_", "-");

        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String objectName = folder + "/" + fileName;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String fileUrl = minioBucketsConfig.getEndpoint() + "/" + bucketName + "/" + objectName;

            return FileUploadResponse.builder()
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error uploading file: ", e);
            throw new RuntimeException("Could not upload file", e);
        }
    }

    private String resolveBucketName(FileCategory category) {
        String bucket = minioBucketsConfig.getBuckets()
                .get(category.name().toLowerCase().replace("_", "-"));
        if (bucket == null) {
            throw new IllegalArgumentException("Bucket not configured for category: " + category);
        }
        return bucket;
    }

    private void validateFile(MultipartFile file, FileCategory category) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();

        switch (category) {
            case PROFILE_IMAGE:
                if (!isValidImageType(contentType)) {
                    throw new IllegalArgumentException("Invalid image type. Only images allowed.");
                }
                break;

            case USER_DOCUMENT:
            case MEDICAL_REPORT:
            case PRESCRIPTION:
                if (!isValidDocumentType(contentType)) {
                    throw new IllegalArgumentException("Invalid document type. Only PDF or DOC allowed.");
                }
                break;
                
            case CONSULTATION_ATTACHMENT:
                // Allow both images and documents for consultation attachments
                if (!isValidImageType(contentType) && !isValidDocumentType(contentType) && !isValidMediaType(contentType)) {
                    throw new IllegalArgumentException("Invalid file type. Only images, documents, and media files allowed for consultation attachments.");
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown category");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10 MB limit");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/webp")
        );
    }

    private boolean isValidDocumentType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }

    private boolean isValidMediaType(String contentType) {
        return contentType != null && (
                contentType.startsWith("video/") ||
                contentType.startsWith("audio/") ||
                contentType.equals("application/zip") ||
                contentType.equals("application/x-zip-compressed") ||
                contentType.equals("text/plain")
        );
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private void ensureBucketExists(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket created: {}", bucketName);

                // Only apply policy when we create the bucket
                applyDefaultPolicy(bucketName);
            } else {
                log.info("Bucket already exists: {}", bucketName);
            }

        } catch (Exception e) {
            log.error("Error ensuring bucket exists: {}", bucketName, e);
            throw new RuntimeException("Could not initialize MinIO bucket " + bucketName, e);
        }
    }

    private void applyDefaultPolicy(String bucketName) {
        try {
            // Example: only make profile-images public by default
            if (bucketName.contains("profile")) {
                String publicPolicy = "{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [\n" +
                        "    {\n" +
                        "      \"Effect\": \"Allow\",\n" +
                        "      \"Principal\": {\"AWS\": \"*\"},\n" +
                        "      \"Action\": \"s3:GetObject\",\n" +
                        "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(publicPolicy)
                                .build()
                );
                log.info("Applied default public policy to bucket: {}", bucketName);
            } else {
                log.info("Default private policy applied implicitly to bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Policy may already exist for bucket {}. Skipping. Details: {}", bucketName, e.getMessage());
        }
    }
}
