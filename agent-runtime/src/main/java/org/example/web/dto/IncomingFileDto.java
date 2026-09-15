package org.example.web.dto;

public record IncomingFileDto(String fileName, String contentType, byte[] content) {
    public IncomingFileDto {
        if (fileName == null || fileName.isBlank()) throw new IllegalArgumentException("fileName darf nicht leer sein");
        content = content == null ? new byte[0] : content.clone();
    }

    @Override
    public byte[] content() { return content.clone(); }
}
