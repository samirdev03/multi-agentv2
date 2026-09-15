package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.agent.AgentRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentToolInitializer {
    private final AgentRepository agentRepository;
    private final ToolRegistry toolRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void assignAllToolsToAgentThree() {
        agentRepository.findById(3L).ifPresent(agent -> {
            agent.setToolIds(new java.util.HashSet<>(toolRegistry.toolIds()));
            agentRepository.save(agent);
        });
    }
}
