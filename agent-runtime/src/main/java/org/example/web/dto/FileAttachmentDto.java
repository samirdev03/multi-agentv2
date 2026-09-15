package org.example.web.dto;

public record FileAttachmentDto(
        String fileName,
        FileType type,
        byte[] content
) {
    public FileAttachmentDto {
        content = content == null ? new byte[0] : content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
