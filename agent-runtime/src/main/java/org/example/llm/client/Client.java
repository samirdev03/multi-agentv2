package org.example.llm.client;

import org.example.agent.AgentEntity;
import org.example.llm.dto.ResponseDto;

public interface Client {
    public ResponseDto getResponse(AgentEntity agent, String userMessage);
}
