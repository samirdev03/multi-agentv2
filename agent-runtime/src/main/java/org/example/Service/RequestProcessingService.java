package org.example.Service;

import lombok.RequiredArgsConstructor;
import org.example.agent.AgentEntity;
import org.example.agent.AgentChannelEntity;
import org.example.agent.AgentService;
import org.example.api.dto.GenericRequestDto;
import org.example.api.dto.GenericResponseDto;
import org.example.api.dto.ResponseDto;
import org.example.callback.CallbackResponseClient;
import org.example.llm.client.Client;
import org.example.llm.client.ClientRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final CallbackResponseClient callbackResponseClient;

    public void process(GenericRequestDto request) {

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
        ResponseDto clientResponse = client.getResponse(agent, request);

        GenericResponseDto response = new GenericResponseDto(
                request.getChannelType(),
                request.getChannelId(),
                clientResponse.getContent(),
                clientResponse.getAttachments()
        );

        callbackResponseClient.sendResponse(request.responseUrl(), response);
    }

    private AgentEntity createDefaultAgent(
            GenericRequestDto request
    ) {

        AgentEntity agent = AgentEntity.builder()
                .name(request.getChannelId())
                .systemPrompt(FALLBACK_PROMPT)
                .provider(DEFAULT_PROVIDER)
                .modelId(DEFAULT_MODEL)
                .build();

        AgentChannelEntity channel = AgentChannelEntity.builder()
                .type(request.getChannelType().name())
                .channelId(request.getChannelId())
                .agent(agent)
                .build();
        agent.setChannels(List.of(channel));

        return agentService.saveAgent(agent);
    }
}
