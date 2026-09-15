package com.example.telegramconnector.domain;

public record TelegramFile(String fileId, String fileName, String contentType, byte[] content) {
    public TelegramFile {
        if (fileId == null || fileId.isBlank()) throw new IllegalArgumentException("fileId darf nicht leer sein");
        if (fileName == null || fileName.isBlank()) fileName = fileId + ".bin";
        content = content == null ? new byte[0] : content.clone();
    }
    @Override public byte[] content() { return content.clone(); }
}
