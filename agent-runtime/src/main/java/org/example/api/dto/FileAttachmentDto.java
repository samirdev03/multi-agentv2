package org.example.api.dto;

public record FileAttachmentDto(
        String path,
        String fileName,
        FileType type
) {
}