package org.example.Service;

import lombok.RequiredArgsConstructor;
import org.example.agent.AgentEntity;
import org.example.agent.AgentService;
import org.example.api.dto.RequestDto;
import org.example.api.dto.ResponseDto;
import org.example.llm.client.Client;
import org.example.llm.client.ClientRegistry;
import org.example.llm.client.channel.Channel;
import org.example.llm.client.channel.ChannelRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestProcessingService {

    private static final String DEFAULT_PROVIDER = "openrouter";

    private static final String DEFAULT_MODEL =
            "nex-agi/nex-n2.5-mini:free";

    private static final String FALLBACK_PROMPT =
            """
            Du bist ein Fallback Agent, der erstellt wurde,
            weil du über einen neuen Channel angesprochen wurdest.
            Teile in deiner ersten Antwort mit, dass dies die
            erste Nachricht an dich als Agent ist.
            """;

    private final AgentService agentService;
    private final ClientRegistry clientRegistry;
    private final ChannelRegistry channelRegistry;

    public void process(RequestDto request) {

        // 1. Passenden Channel Client laden
        Channel channel = channelRegistry.getChannel(
                request.getChannelType()
        );

        // 2. Agent laden oder erstellen
        AgentEntity agent = agentService
                .getAgentByChannel(
                        request.getChannelType(),
                        request.getChannelId()
                )
                .orElseGet(() ->
                        createDefaultAgent(request)
                );

        // 3. Provider Client laden
        Client client = clientRegistry.getClient(
                agent.getProvider()
        );

        // 4. LLM Response holen
        String content = client
                .getResponse(
                        agent,
                        request
                )
                .content();

        // 5. Channel-spezifische Response bauen
        ResponseDto response = channel.buildResponse(
                content,
                request.getChannelId()
        );

        // 6. Über denselben Channel zurücksenden
        channel.sendResponse(response);
    }

    private AgentEntity createDefaultAgent(
            RequestDto request
    ) {

        AgentEntity agent = AgentEntity.builder()
                .name(request.getChannelId())
                .systemPrompt(FALLBACK_PROMPT)
                .provider(DEFAULT_PROVIDER)
                .modelId(DEFAULT_MODEL)
                .build();

        agent.addChannel(
                request.getChannelType(),
                request.getChannelId()
        );

        return agentService.saveAgent(agent);
    }
}