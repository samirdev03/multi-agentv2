package com.example.telegramconnector.api;

import java.util.List;
import java.util.UUID;

public record GenericResponseRequest(
        String channelType,
        String channelId,
        String content,
        List<FileAttachmentRequest> attachments, UUID requestId) {
    public GenericResponseRequest(String channelType, String channelId, String content, List<FileAttachmentRequest> attachments) { this(channelType, channelId, content, attachments, null); }
}
