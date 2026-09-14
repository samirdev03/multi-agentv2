package com.example.telegramconnector.api;

public record FileAttachmentRequest(String fileName, FileType type, byte[] content) {
    public FileAttachmentRequest {
        content = content == null ? new byte[0] : content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
