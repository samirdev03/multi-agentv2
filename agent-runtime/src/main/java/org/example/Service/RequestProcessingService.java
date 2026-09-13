package org.example.Service;

import lombok.RequiredArgsConstructor;
import org.example.agent.AgentEntity;
import org.example.agent.AgentService;
import org.example.api.dto.TelegramMessageDto;
import org.example.llm.client.Client;
import org.example.llm.client.ClientRegistry;
import org.example.llm.client.channel.ChannelRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestProcessingService {

    private final AgentService agentService;
    private final ClientRegistry clientRegistry;
    private final ChannelRegistry channelRegistry;
    private static final String DEFAULT_PROVIDER = "openrouter";
    private static final String FALLBACK_PROMPT = "Du bist ein Fallback Agent der erstellt wurde, weil du über einen neuen Channel angesprochen wurdest." +
            "Teile in deiner ersten Antwort bitte mit, dass dies die erste Nachricht an dich ist als Agent.";
    private static final String DEFAULT_MODEL = "nex-agi/nex-n2.5-mini:free";

    public void getTelegramResponse(TelegramMessageDto msg){
        AgentEntity agent = agentService.getAgentByChannelId(msg.channelId())
                        .orElseGet(() -> AgentEntity.builder()
                                .name(msg.channelId())
                                .systemPrompt(FALLBACK_PROMPT)
                                .channelId(msg.channelId())
                                .provider(DEFAULT_PROVIDER)
                                .modelId(DEFAULT_MODEL)
                                .build());
        agentService.saveAgent(agent);
        String response =  getClient(agent.getProvider()).getResponse(agent, msg.message()).content();
        sendTelegramResponse(buildTelegramResponse(response, msg.channelId()));

    }

    private TelegramMessageDto buildTelegramResponse(String message, String channelId){
        return new TelegramMessageDto(message, channelId);
    }
    private void sendTelegramResponse(TelegramMessageDto response){
        channelRegistry.getTelegram().sendMessage(response);
    }

    private Client getClient(String provider){
        return clientRegistry.getClient(provider);
    }


}
