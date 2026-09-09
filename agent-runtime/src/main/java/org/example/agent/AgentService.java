package org.example.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    public void saveAgent(AgentEntity agent){
        agentRepository.save(agent);
    }
    public Optional<AgentEntity> getAgentById(Long id){
        return agentRepository.findById(id);
    }
    public Optional<AgentEntity> getAgentByChannelId(String channelId){
        return agentRepository.findByChannelId(channelId);
    }



}
