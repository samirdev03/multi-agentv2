package org.example.agent;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.ChannelType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    public AgentEntity saveAgent(AgentEntity agent) {
        return agentRepository.save(agent);
    }

    public Optional<AgentEntity> getAgentById(Long id) {
        return agentRepository.findById(id);
    }

    public Optional<AgentEntity> getAgentByChannel(
            ChannelType channelType,
            String channelId
    ) {
        return agentRepository.findByChannel(
                channelType,
                channelId
        );
    }
}