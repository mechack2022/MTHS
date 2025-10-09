package com.auth.service.entity;

import com.auth.service.constants.MessageType;
import com.auth.service.constants.MessageStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@EqualsAndHashCode(callSuper = false)
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_consultation_id", nullable = false)
    private VideoConsultation videoConsultation;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_type", nullable = false)
    private String senderType; // PATIENT, DOCTOR

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "receiver_type", nullable = false)
    private String receiverType; // PATIENT, DOCTOR

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_edited")
    private Boolean isEdited = false;

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "attachment_type")
    private String attachmentType;

    @Column(name = "attachment_size")
    private Long attachmentSize;

    @Column(name = "is_system_message")
    private Boolean isSystemMessage = false;

    // Helper methods
    public boolean isRead() {
        return readAt != null;
    }

    public boolean isDelivered() {
        return deliveredAt != null;
    }

    public Long getVideoConsultationId() {
        return videoConsultation != null ? videoConsultation.getId() : null;
    }

    public void markAsDelivered() {
        if (this.deliveredAt == null) {
            this.deliveredAt = LocalDateTime.now();
            this.status = MessageStatus.DELIVERED;
        }
    }

    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
            this.status = MessageStatus.READ;
            if (this.deliveredAt == null) {
                this.deliveredAt = LocalDateTime.now();
            }
        }
    }

    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.trim().isEmpty();
    }
}