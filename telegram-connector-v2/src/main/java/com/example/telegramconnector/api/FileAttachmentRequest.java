package com.example.telegramconnector.api;

public record FileAttachmentRequest(String path, String fileName, FileType type) {
}
