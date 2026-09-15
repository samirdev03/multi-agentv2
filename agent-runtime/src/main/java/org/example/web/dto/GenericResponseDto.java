package org.example.web.dto;

import java.util.List;
import java.util.UUID;

public record GenericResponseDto(
        ChannelType channelType,
        String channelId,
        String content,
        List<FileAttachmentDto> attachments,
        UUID requestId
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
        this(channelType, channelId, content, List.of(), null);
    }

    public GenericResponseDto(ChannelType channelType, String channelId, String content, List<FileAttachmentDto> attachments) {
        this(channelType, channelId, content, attachments, null);
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
