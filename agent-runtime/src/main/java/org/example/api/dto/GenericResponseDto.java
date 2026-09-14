package org.example.api.dto;

import java.util.List;

public record GenericResponseDto(
        ChannelType channelType,
        String channelId,
        String content,
        List<FileAttachmentDto> attachments
) implements ResponseDto {

    public GenericResponseDto {
        attachments = attachments == null
                ? List.of()
                : List.copyOf(attachments);
    }

    public GenericResponseDto(
            ChannelType channelType,
            String channelId,
            String content
    ) {
        this(channelType, channelId, content, List.of());
    }

    @Override
    public ChannelType getChannelType() {
        return channelType;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public List<FileAttachmentDto> getAttachments() {
        return attachments;
    }
}