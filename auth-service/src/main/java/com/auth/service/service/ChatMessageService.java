package com.auth.service.service;

import com.auth.service.controller.ChatController;
import com.auth.service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatMessageService {
    ChatMessageDTO sendMessage(Long consultationId, ChatController.SendMessageRequest request);
    ChatMessageDTO sendMessageWithAttachment(Long consultationId, ChatController.SendMessageRequest request, MultipartFile attachment);
    
    // Message retrieval methods
    Page<ChatMessageDTO> getMessages(Long consultationId, Pageable pageable, Long beforeMessageId);
    List<ChatMessageDTO> getAllMessages(Long consultationId);
    List<ChatMessageDTO> getConsultationMessages(Long consultationId, int page, int size);
    
    // Message status methods
    ChatMessageDTO markMessageAsRead(Long messageId, Long userId);
    void markAsRead(Long messageId, Long userId);
    void markAllMessagesAsRead(Long consultationId, Long userId);
    Long getUnreadMessageCount(Long consultationId, Long userId);
    List<ChatMessageDTO> getUnreadMessages(Long consultationId, Long userId);
    
    // Message editing and deletion
    ChatMessageDTO editMessage(Long messageId, ChatController.EditMessageRequest request);
    void deleteMessage(Long messageId, Long userId);
    ChatMessageDTO updateMessageStatus(Long messageId, String status);
    
    // Search methods
    List<ChatMessageDTO> searchMessages(Long consultationId, String searchQuery);
    Page<ChatMessageDTO> searchMessages(Long consultationId, String query, Pageable pageable);
    
    // File handling methods
    FileAttachmentDTO uploadFile(Long consultationId, MultipartFile file, Long uploaderId, String uploaderType, String description, Boolean isMedicalRecord);
    FileAttachmentDTO uploadAttachment(Long consultationId, MultipartFile file, Long uploadedById, String uploadedByType);
    List<FileAttachmentDTO> getFiles(Long consultationId, String fileType);
    void deleteFile(Long fileId, Long userId);
    
    // Utility methods
    void sendTypingIndicator(Long consultationId, Long userId, boolean isTyping);
}