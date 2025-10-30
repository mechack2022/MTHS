package com.mths.consultation.service;

import com.mths.auth.dto.*;
import com.mths.auth.entity.*;
import com.mths.auth.service.FileUploadService;
import com.mths.consultation.controller.ChatController;
import com.mths.consultation.dto.ChatMessageDTO;
import com.mths.consultation.dto.FileAttachmentDTO;
import com.mths.consultation.entity.ChatMessage;
import com.mths.consultation.entity.FileAttachment;
import com.mths.consultation.entity.VideoConsultation;
import com.mths.consultation.repository.ChatMessageRepository;
import com.mths.consultation.repository.FileAttachmentRepository;
import com.mths.consultation.repository.VideoConsultationRepository;
import com.mths.shared.exceptions.ResourceNotFoundException;
import com.mths.shared.mapper.ChatMessageMapper;
import com.mths.shared.mapper.FileAttachmentMapper;
import com.mths.auth.repository.*;
import com.mths.shared.websocket.VideoConsultationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final VideoConsultationRepository videoConsultationRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final FileAttachmentMapper fileAttachmentMapper;
    private final VideoConsultationWebSocketHandler webSocketHandler;
    private final FileUploadService fileUploadService;

    @Override
    public ChatMessageDTO sendMessage(Long consultationId, ChatController.SendMessageRequest request) {
        log.info("Sending message to consultation: {}", consultationId);
        
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        User sender = userRepository.findById(request.senderId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.senderId()));

        ChatMessage message = new ChatMessage();
        message.setVideoConsultation(consultation);
        message.setSenderId(request.senderId());
        message.setSenderType(request.senderType());
        message.setReceiverId(request.receiverId());
        message.setReceiverType(request.receiverType());
        message.setContent(request.content());
        message.setMessageType(request.messageType() != null ? 
            com.mths.shared.constants.MessageType.valueOf(request.messageType()) : 
            com.mths.shared.constants.MessageType.TEXT);
        message.setStatus(com.mths.shared.constants.MessageStatus.SENT);
        message.setSentAt(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageDTO messageDTO = chatMessageMapper.toDTO(saved);
        
        // Send real-time message via WebSocket
        Map<String, Object> websocketMessage = Map.of(
            "type", "NEW_MESSAGE",
            "message", messageDTO,
            "consultationId", consultation.getSessionId(),
            "timestamp", System.currentTimeMillis()
        );
        
        webSocketHandler.notifyConsultationUpdate(consultation.getSessionId(), websocketMessage);
        
        log.info("Message sent successfully with ID: {}", saved.getId());
        return messageDTO;
    }

    @Override
    public ChatMessageDTO sendMessageWithAttachment(Long consultationId, ChatController.SendMessageRequest request, MultipartFile attachment) {
        log.info("Sending message with attachment to consultation: {}", consultationId);
        
        // First, upload the attachment
        FileAttachmentDTO attachmentDTO = uploadAttachment(
            consultationId, 
            attachment, 
            request.senderId(), 
            request.senderType()
        );
        
        // Create message with attachment reference
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        User sender = userRepository.findById(request.senderId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.senderId()));

        ChatMessage message = new ChatMessage();
        message.setVideoConsultation(consultation);
        message.setSenderId(request.senderId());
        message.setSenderType(request.senderType());
        message.setReceiverId(request.receiverId());
        message.setReceiverType(request.receiverType());
        message.setContent(request.content() != null ? request.content() : "File attachment");
        message.setMessageType(com.mths.shared.constants.MessageType.FILE);
        message.setStatus(com.mths.shared.constants.MessageStatus.SENT);
        message.setSentAt(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(message);
        
        // Link attachment to message
        FileAttachment fileAttachment = fileAttachmentRepository.findById(attachmentDTO.getId())
            .orElseThrow(() -> new ResourceNotFoundException("FileAttachment", "id", attachmentDTO.getId()));
        fileAttachment.setChatMessage(saved);
        fileAttachmentRepository.save(fileAttachment);
        
        ChatMessageDTO messageDTO = chatMessageMapper.toDTO(saved);
        messageDTO.setAttachmentUrl(attachmentDTO.getFileUrl());
        messageDTO.setAttachmentType(attachmentDTO.getFileType());
        messageDTO.setAttachmentSize(attachmentDTO.getFileSize());
        messageDTO.setHasAttachment(true);
        
        // Send real-time message via WebSocket
        Map<String, Object> websocketMessage = Map.of(
            "type", "NEW_MESSAGE",
            "message", messageDTO,
            "consultationId", consultation.getSessionId(),
            "timestamp", System.currentTimeMillis()
        );
        
        webSocketHandler.notifyConsultationUpdate(consultation.getSessionId(), websocketMessage);
        
        log.info("Message with attachment sent successfully with ID: {}", saved.getId());
        return messageDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConsultationMessages(Long consultationId, int page, int size) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ChatMessage> messagesPage = chatMessageRepository.findByVideoConsultation(consultation, pageable);
        
        return messagesPage.getContent().stream()
            .map(chatMessageMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ChatMessageDTO markMessageAsRead(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        // Only mark as read if the user is not the sender
        if (!message.getSenderId().equals(userId)) {
            message.setReadAt(LocalDateTime.now());
            message.setStatus(com.mths.shared.constants.MessageStatus.READ);
            ChatMessage saved = chatMessageRepository.save(message);
            
            // Notify sender about read receipt
            Map<String, Object> readReceipt = Map.of(
                "type", "MESSAGE_READ",
                "messageId", messageId,
                "readBy", userId,
                "timestamp", System.currentTimeMillis()
            );
            
            webSocketHandler.notifyConsultationUpdate(
                message.getVideoConsultation().getSessionId(), 
                readReceipt
            );
            
            return chatMessageMapper.toDTO(saved);
        }
        
        return chatMessageMapper.toDTO(message);
    }

    @Override
    public void markAllMessagesAsRead(Long consultationId, Long userId) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByConsultationAndNotSender(consultation, userId);
        
        LocalDateTime now = LocalDateTime.now();
        unreadMessages.forEach(message -> {
            message.setReadAt(now);
            message.setStatus(com.mths.shared.constants.MessageStatus.READ);
        });
        
        chatMessageRepository.saveAll(unreadMessages);
        
        // Notify about bulk read receipt
        Map<String, Object> bulkReadReceipt = Map.of(
            "type", "MESSAGES_READ",
            "consultationId", consultationId,
            "readBy", userId,
            "count", unreadMessages.size(),
            "timestamp", System.currentTimeMillis()
        );
        
        webSocketHandler.notifyConsultationUpdate(consultation.getSessionId(), bulkReadReceipt);
        
        log.info("Marked {} messages as read for user {} in consultation {}", 
                unreadMessages.size(), userId, consultationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> searchMessages(Long consultationId, String searchQuery) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        List<ChatMessage> messages = chatMessageRepository.searchMessagesByContent(consultation, searchQuery);
        
        return messages.stream()
            .map(chatMessageMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ChatMessageDTO updateMessageStatus(Long messageId, String status) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        message.setStatus(com.mths.shared.constants.MessageStatus.valueOf(status.toUpperCase()));
        message.setEditedAt(LocalDateTime.now());
        
        if ("delivered".equals(status)) {
            message.setDeliveredAt(LocalDateTime.now());
        } else if ("read".equals(status)) {
            message.setReadAt(LocalDateTime.now());
        }
        
        ChatMessage saved = chatMessageRepository.save(message);
        return chatMessageMapper.toDTO(saved);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        // Only allow sender to delete their own messages
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("User can only delete their own messages");
        }

        message.setDeleted(true);
        message.setUpdatedAt(LocalDateTime.now());
        chatMessageRepository.save(message);
        
        // Notify about message deletion
        Map<String, Object> deleteNotification = Map.of(
            "type", "MESSAGE_DELETED",
            "messageId", messageId,
            "deletedBy", userId,
            "timestamp", System.currentTimeMillis()
        );
        
        webSocketHandler.notifyConsultationUpdate(
            message.getVideoConsultation().getSessionId(),
            deleteNotification
        );
        
        log.info("Message {} deleted by user {}", messageId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getUnreadMessages(Long consultationId, Long userId) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByConsultationAndNotSender(consultation, userId);
        
        return unreadMessages.stream()
            .map(chatMessageMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(Long consultationId, Long userId) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        return (long) chatMessageRepository.countUnreadMessagesByConsultationAndNotSender(consultation, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getMessages(Long consultationId, Pageable pageable, Long beforeMessageId) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        Page<ChatMessage> messagesPage;
        if (beforeMessageId != null) {
            // For pagination with cursor-based approach
            messagesPage = chatMessageRepository.findByVideoConsultationAndIdLessThanOrderByCreatedAtDesc(
                consultation, beforeMessageId, pageable);
        } else {
            messagesPage = chatMessageRepository.findByVideoConsultation(consultation, pageable);
        }
        
        return messagesPage.map(chatMessageMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getAllMessages(Long consultationId) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        List<ChatMessage> messages = chatMessageRepository.findByVideoConsultationOrderBySentAtAsc(consultation);
        return messages.stream()
            .map(chatMessageMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        markMessageAsRead(messageId, userId);
    }

    @Override
    public ChatMessageDTO editMessage(Long messageId, ChatController.EditMessageRequest request) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        // Only allow sender to edit their own messages
        if (!message.getSenderId().equals(request.userId())) {
            throw new IllegalArgumentException("User can only edit their own messages");
        }

        message.setContent(request.content());
        message.setEditedAt(LocalDateTime.now());
        message.setIsEdited(true);
        ChatMessage saved = chatMessageRepository.save(message);
        
        // Notify about message edit
        Map<String, Object> editNotification = Map.of(
            "type", "MESSAGE_EDITED",
            "messageId", messageId,
            "newContent", request.content(),
            "editedBy", request.userId(),
            "timestamp", System.currentTimeMillis()
        );
        
        webSocketHandler.notifyConsultationUpdate(
            message.getVideoConsultation().getSessionId(),
            editNotification
        );
        
        return chatMessageMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> searchMessages(Long consultationId, String query, Pageable pageable) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        // For simplicity, using the existing search method and converting to Page
        List<ChatMessage> messages = chatMessageRepository.searchMessagesByContent(consultation, query);
        
        // Manual pagination (in a real app, you'd want database-level pagination)
        int start = Math.toIntExact(pageable.getOffset());
        int end = Math.min((start + pageable.getPageSize()), messages.size());
        List<ChatMessage> pageContent = messages.subList(start, end);
        
        Page<ChatMessage> messagesPage = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, messages.size());
        
        return messagesPage.map(chatMessageMapper::toDTO);
    }

    @Override
    public FileAttachmentDTO uploadFile(Long consultationId, MultipartFile file, Long uploaderId, String uploaderType, String description, Boolean isMedicalRecord) {
        log.info("Uploading file for consultation: {}", consultationId);
        
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        User uploader = userRepository.findById(uploaderId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploaderId));

        try {
            // Upload file using existing file upload service
            FileUploadResponse uploadResponse = fileUploadService.uploadFile(file, com.mths.shared.constants.FileCategory.CONSULTATION_ATTACHMENT);
            
            // Create file attachment record
            FileAttachment attachment = new FileAttachment();
            attachment.setVideoConsultation(consultation);
            attachment.setOriginalFilename(uploadResponse.getOriginalFileName());
            attachment.setStoredFilename(uploadResponse.getFileName());
            attachment.setFileUrl(uploadResponse.getFileUrl());
            attachment.setFileType(getFileType(uploadResponse.getContentType()));
            attachment.setFileSize(uploadResponse.getFileSize());
            attachment.setMimeType(uploadResponse.getContentType());
            attachment.setUploadedById(uploaderId);
            attachment.setUploadedByType(uploaderType);
            attachment.setIsMedicalRecord(isMedicalRecord != null ? isMedicalRecord : isMedicalRecord(uploadResponse.getOriginalFileName()));
            attachment.setDescription(description);
            
            FileAttachment saved = fileAttachmentRepository.save(attachment);
            
            FileAttachmentDTO dto = fileAttachmentMapper.toDTO(saved);
            dto.setUploadedByName(uploader.getFirstName() + " " + uploader.getLastName());
            
            log.info("File uploaded successfully with ID: {}", saved.getId());
            return dto;
            
        } catch (Exception e) {
            log.error("Error uploading file", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileAttachmentDTO> getFiles(Long consultationId, String fileType) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        List<FileAttachment> attachments;
        if (fileType != null && !fileType.trim().isEmpty()) {
            attachments = fileAttachmentRepository.findByConsultationAndFileType(consultation, fileType.toUpperCase());
        } else {
            attachments = fileAttachmentRepository.findByVideoConsultation(consultation);
        }
        
        return attachments.stream()
            .map(attachment -> {
                FileAttachmentDTO dto = fileAttachmentMapper.toDTO(attachment);
                // Get uploader name from user repository
                try {
                    User uploader = userRepository.findById(attachment.getUploadedById()).orElse(null);
                    if (uploader != null) {
                        dto.setUploadedByName(uploader.getFirstName() + " " + uploader.getLastName());
                    }
                } catch (Exception e) {
                    log.warn("Could not load uploader name for attachment {}: {}", attachment.getId(), e.getMessage());
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(Long fileId, Long userId) {
        FileAttachment attachment = fileAttachmentRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("FileAttachment", "id", fileId));

        // Only allow uploader or admin to delete files
        if (!attachment.getUploadedById().equals(userId)) {
            throw new IllegalArgumentException("User can only delete their own files");
        }

        // TODO: Implement file deletion from MinIO storage
        // The FileUploadService doesn't currently have a deleteFile method
        log.info("File deletion from storage not implemented yet for file: {}", attachment.getStoredFilename());

        fileAttachmentRepository.delete(attachment);
        
        // Notify about file deletion
        Map<String, Object> deleteNotification = Map.of(
            "type", "FILE_DELETED",
            "fileId", fileId,
            "deletedBy", userId,
            "timestamp", System.currentTimeMillis()
        );
        
        webSocketHandler.notifyConsultationUpdate(
            attachment.getVideoConsultation().getSessionId(),
            deleteNotification
        );
        
        log.info("File {} deleted by user {}", fileId, userId);
    }

    @Override
    public FileAttachmentDTO uploadAttachment(Long consultationId, MultipartFile file, Long uploadedById, String uploadedByType) {
        log.info("Uploading attachment for consultation: {}", consultationId);
        
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        User uploader = userRepository.findById(uploadedById)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploadedById));

        try {
            // Upload file using existing file upload service
          FileUploadResponse uploadResponse = fileUploadService.uploadFile(file, com.mths.shared.constants.FileCategory.CONSULTATION_ATTACHMENT);
            
            // Create file attachment record
            FileAttachment attachment = new FileAttachment();
            attachment.setVideoConsultation(consultation);
            attachment.setOriginalFilename(uploadResponse.getOriginalFileName());
            attachment.setStoredFilename(uploadResponse.getFileName());
            attachment.setFileUrl(uploadResponse.getFileUrl());
            attachment.setFileType(getFileType(uploadResponse.getContentType()));
            attachment.setFileSize(uploadResponse.getFileSize());
            attachment.setMimeType(uploadResponse.getContentType());
            attachment.setUploadedById(uploadedById);
            attachment.setUploadedByType(uploadedByType);
            attachment.setIsMedicalRecord(isMedicalRecord(uploadResponse.getOriginalFileName()));
            
            FileAttachment saved = fileAttachmentRepository.save(attachment);
            
            FileAttachmentDTO dto = fileAttachmentMapper.toDTO(saved);
            dto.setUploadedByName(uploader.getFirstName() + " " + uploader.getLastName());
            
            log.info("File attachment uploaded successfully with ID: {}", saved.getId());
            return dto;
            
        } catch (Exception e) {
            log.error("Error uploading file attachment", e);
            throw new RuntimeException("Failed to upload file attachment: " + e.getMessage());
        }
    }

    @Override
    public void sendTypingIndicator(Long consultationId, Long userId, boolean isTyping) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        Map<String, Object> typingIndicator = Map.of(
            "type", "TYPING_INDICATOR",
            "userId", userId,
            "isTyping", isTyping,
            "consultationId", consultationId,
            "timestamp", System.currentTimeMillis()
        );
        
        webSocketHandler.notifyConsultationUpdate(consultation.getSessionId(), typingIndicator);
    }


    private String getFileType(String mimeType) {
        if (mimeType == null) return "OTHER";
        
        if (mimeType.startsWith("image/")) return "IMAGE";
        if (mimeType.startsWith("video/")) return "VIDEO";
        if (mimeType.startsWith("audio/")) return "AUDIO";
        if (mimeType.equals("application/pdf")) return "PDF";
        if (mimeType.contains("document") || mimeType.contains("word") || mimeType.contains("text")) return "DOCUMENT";
        
        return "OTHER";
    }

    private Boolean isMedicalRecord(String filename) {
        if (filename == null) return false;
        
        String lowerName = filename.toLowerCase();
        return lowerName.contains("medical") || 
               lowerName.contains("report") || 
               lowerName.contains("prescription") ||
               lowerName.contains("test") ||
               lowerName.contains("lab") ||
               lowerName.contains("xray") ||
               lowerName.contains("scan");
    }
}