package com.auth.service.controller;

import com.auth.service.dto.*;
import com.auth.service.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    // ========================================================================
    // CHAT MESSAGE MANAGEMENT
    // ========================================================================

    @PostMapping("/consultations/{consultationId}/messages")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(
            @PathVariable Long consultationId,
            @Valid @RequestBody SendMessageRequest request) {
        
        log.info("Sending message in consultation: {}", consultationId);

        ChatMessageDTO message = chatMessageService.sendMessage(consultationId, request);

        ApiResponse<ChatMessageDTO> response = ApiResponse.success(
                "Message sent successfully",
                message,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/consultations/{consultationId}/messages")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getMessages(
            @PathVariable Long consultationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long beforeMessageId) {
        
        if (size > 0) {
            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessageDTO> messages = chatMessageService.getMessages(consultationId, pageable, beforeMessageId);
            
            ApiResponse<List<ChatMessageDTO>> response = ApiResponse.success(
                    "Messages retrieved successfully",
                    messages.getContent(),
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        } else {
            List<ChatMessageDTO> messages = chatMessageService.getAllMessages(consultationId);
            
            ApiResponse<List<ChatMessageDTO>> response = ApiResponse.success(
                    "Messages retrieved successfully",
                    messages,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/messages/{messageId}/read")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> markMessageAsRead(
            @PathVariable Long messageId,
            @Valid @RequestBody MarkAsReadRequest request) {
        
        chatMessageService.markAsRead(messageId, request.userId());

        ApiResponse<String> response = ApiResponse.success(
                "Message marked as read successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/messages/{messageId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> editMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody EditMessageRequest request) {
        
        log.info("Editing message: {}", messageId);

        ChatMessageDTO message = chatMessageService.editMessage(messageId, request);

        ApiResponse<ChatMessageDTO> response = ApiResponse.success(
                "Message edited successfully",
                message,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        
        log.info("Deleting message: {} by user: {}", messageId, userId);

        chatMessageService.deleteMessage(messageId, userId);

        ApiResponse<String> response = ApiResponse.success(
                "Message deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // FILE SHARING
    // ========================================================================

    @PostMapping("/consultations/{consultationId}/files")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<FileAttachmentDTO>> uploadFile(
            @PathVariable Long consultationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long uploaderId,
            @RequestParam String uploaderType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") Boolean isMedicalRecord) {
        
        log.info("Uploading file for consultation: {}", consultationId);

        FileAttachmentDTO attachment = chatMessageService.uploadFile(
                consultationId, file, uploaderId, uploaderType, description, isMedicalRecord);

        ApiResponse<FileAttachmentDTO> response = ApiResponse.success(
                "File uploaded successfully",
                attachment,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/consultations/{consultationId}/files")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FileAttachmentDTO>>> getFiles(
            @PathVariable Long consultationId,
            @RequestParam(required = false) String fileType) {
        
        List<FileAttachmentDTO> files = chatMessageService.getFiles(consultationId, fileType);

        ApiResponse<List<FileAttachmentDTO>> response = ApiResponse.success(
                "Files retrieved successfully",
                files,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/files/{fileId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable Long fileId,
            @RequestParam Long userId) {
        
        log.info("Deleting file: {} by user: {}", fileId, userId);

        chatMessageService.deleteFile(fileId, userId);

        ApiResponse<String> response = ApiResponse.success(
                "File deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // CHAT STATISTICS
    // ========================================================================

    @GetMapping("/consultations/{consultationId}/unread-count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
            @PathVariable Long consultationId,
            @RequestParam Long userId) {
        
        Long unreadCount = chatMessageService.getUnreadMessageCount(consultationId, userId);

        ApiResponse<Long> response = ApiResponse.success(
                "Unread count retrieved successfully",
                unreadCount,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/consultations/{consultationId}/mark-all-read")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> markAllMessagesAsRead(
            @PathVariable Long consultationId,
            @RequestParam Long userId) {
        
        chatMessageService.markAllMessagesAsRead(consultationId, userId);

        ApiResponse<String> response = ApiResponse.success(
                "All messages marked as read successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // SEARCH AND FILTERING
    // ========================================================================

    @GetMapping("/consultations/{consultationId}/search")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> searchMessages(
            @PathVariable Long consultationId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageDTO> messages = chatMessageService.searchMessages(consultationId, query, pageable);

        ApiResponse<List<ChatMessageDTO>> response = ApiResponse.success(
                "Search results retrieved successfully",
                messages.getContent(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // REQUEST DTOs
    // ========================================================================

    public record SendMessageRequest(
            Long senderId,
            String senderType,
            Long receiverId,
            String receiverType,
            String content,
            String messageType,
            Long replyToMessageId
    ) {}

    public record MarkAsReadRequest(
            Long userId

    ) {}

    public record EditMessageRequest(
            Long userId,
            String content
    ) {}
}