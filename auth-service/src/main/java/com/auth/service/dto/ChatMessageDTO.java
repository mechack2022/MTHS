package com.auth.service.dto;

import com.auth.service.constants.MessageType;
import com.auth.service.constants.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;
    private Long videoConsultationId;
    private Long senderId;
    private String senderType;
    private String senderName;
    private String senderImageUrl;
    private Long receiverId;
    private String receiverType;
    private String receiverName;
    private MessageType messageType;
    private String content;
    private MessageStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime editedAt;
    private Boolean isEdited;
    private Long replyToMessageId;
    private String attachmentUrl;
    private String attachmentType;
    private Long attachmentSize;
    private Boolean isSystemMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Reply message details (if replying to a message)
    private String replyToContent;
    private String replyToSenderName;
    
    // Status indicators
    private Boolean isRead;
    private Boolean isDelivered;
    private Boolean hasAttachment;
    
    // Time formatting for UI
    private String timeFormatted;
    private String dateFormatted;
}