package com.mths.shared.mapper;

import com.mths.consultation.dto.ChatMessageDTO;
import com.mths.consultation.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {FileAttachmentMapper.class})
public interface ChatMessageMapper {

    @Mapping(target = "videoConsultationId", source = "videoConsultation.id")
    @Mapping(target = "senderId", source = "senderId")
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "senderImageUrl", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    @Mapping(target = "replyToContent", ignore = true)
    @Mapping(target = "replyToSenderName", ignore = true)
    @Mapping(target = "isRead", expression = "java(chatMessage.isRead())")
    @Mapping(target = "isDelivered", expression = "java(chatMessage.isDelivered())")
    @Mapping(target = "hasAttachment", expression = "java(chatMessage.hasAttachment())")
    @Mapping(target = "timeFormatted", ignore = true)
    @Mapping(target = "dateFormatted", ignore = true)
    ChatMessageDTO toDTO(ChatMessage chatMessage);

    @Mapping(target = "videoConsultation", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ChatMessage toEntity(ChatMessageDTO dto);

}