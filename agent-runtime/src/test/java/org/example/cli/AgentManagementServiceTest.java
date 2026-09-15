package org.example.cli;

import org.example.agent.AgentEntity;
import org.example.agent.AgentRepository;
import org.example.web.dto.ChannelType;
import org.example.tools.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentManagementServiceTest {
    @Mock AgentRepository repository;
    @Mock ToolRegistry toolRegistry;

    @Test
    void createPersistsAgentAndChannelTogether() {
        when(repository.existsByChannels_TypeAndChannels_ChannelId("TELEGRAM", "chat-1")).thenReturn(false);
        when(repository.save(any(AgentEntity.class))).thenAnswer(invocation -> {
            AgentEntity agent = invocation.getArgument(0);
            agent.setAgentId(7L);
            return agent;
        });

        when(toolRegistry.get("datetime")).thenReturn(null);
        when(toolRegistry.get("sendfiles")).thenReturn(null);
        AgentEntity result = new AgentManagementService(repository, toolRegistry).create(
                "Support", "You help users", "openrouter", "model-x",
                ChannelType.TELEGRAM, "chat-1", "http://connector/responses/chat-1", "datetime, sendfiles");

        assertThat(result.getAgentId()).isEqualTo(7L);
        assertThat(result.getChannels()).singleElement().satisfies(channel -> {
            assertThat(channel.getType()).isEqualTo("TELEGRAM");
            assertThat(channel.getChannelId()).isEqualTo("chat-1");
        });
        assertThat(result.getToolIds()).containsExactlyInAnyOrder("datetime", "sendfiles");
    }
}
