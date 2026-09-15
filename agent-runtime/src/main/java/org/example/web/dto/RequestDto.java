package org.example.web.dto;

import java.net.URI;

public interface RequestDto {

    ChannelType getChannelType();

    String getChannelId();

    String getContent();

    URI responseUrl();
}
