package org.example.llm.client.channel;

import org.example.api.dto.ChannelType;
import org.example.api.dto.ResponseDto;

public interface Channel {

    ChannelType getType();

    ResponseDto buildResponse(
            String content,
            String channelId
    );

    void sendResponse(ResponseDto response);
}