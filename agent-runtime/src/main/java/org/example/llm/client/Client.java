package org.example.llm.client;

import org.example.agent.AgentEntity;
import org.example.api.dto.RequestDto;
import org.example.api.dto.ResponseDto;

public interface Client {
    public ResponseDto getResponse(AgentEntity agent, RequestDto requestDto);
}
