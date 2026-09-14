package com.example.telegramconnector.api;

import java.util.List;

public record GenericResponseRequest(
        String channelType,
        String channelId,
        String content,
        List<FileAttachmentRequest> attachments) {
}
