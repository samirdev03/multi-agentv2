package org.example.llm.client;

import org.example.agent.AgentEntity;
import org.example.web.dto.RequestDto;
import org.example.web.dto.ResponseDto;

public interface Client {
    public ResponseDto getResponse(AgentEntity agent, RequestDto requestDto);
}
