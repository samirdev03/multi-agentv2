package org.example.web.dto;

import java.util.List;

public interface ResponseDto {

    ChannelType getChannelType();

    String getChannelId();

    String getContent();

    List<FileAttachmentDto> getAttachments();
}