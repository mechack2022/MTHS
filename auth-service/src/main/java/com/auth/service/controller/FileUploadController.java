package com.auth.service.controller;

import com.auth.service.constants.FileCategory;
import com.auth.service.dto.ApiResponse;
import com.auth.service.dto.FileUploadResponse;
import com.auth.service.service.FileUploadService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("api/v1/fileUpload")
@AllArgsConstructor
@RestController
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'PENDING')")
    @PostMapping("/profile-image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileUploadService.uploadFile(file, FileCategory.PROFILE_IMAGE);
        return ResponseEntity.ok(
                ApiResponse.success("profile image uploaded successfully",
                        response
                )
        );

    }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'PENDING')")
    @PostMapping("/user-document")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadLicenseDocument(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileUploadService.uploadFile(file,  FileCategory.USER_DOCUMENT);
        return ResponseEntity.ok(
                ApiResponse.success(FileCategory.USER_DOCUMENT.name() + "uploaded successfully",
                        response
                )
        );
    }


}
