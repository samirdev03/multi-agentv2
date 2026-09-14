package org.example.llm.client;

import lombok.RequiredArgsConstructor;
import org.example.agent.AgentEntity;
import org.example.api.dto.GenericResponseDto;
import org.example.api.dto.RequestDto;
import org.example.api.dto.ResponseDto;
import org.example.config.Credential;
import org.example.config.CredentialRegistry;
import org.example.tools.ToolRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenRouterClient implements Client {

    private final CredentialRegistry credentialRegistry;
    private final ToolRegistry toolRegistry;

    @Override
    public ResponseDto getResponse(
            AgentEntity agent,
            RequestDto request
    ) {

        Credential credential =
                credentialRegistry.getRequired("openrouter");

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(credential.baseUrl())
                .apiKey(credential.apiKey())
                .completionsPath("/chat/completions")
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(agent.getModelId())
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        ChatClient chatClient = ChatClient.create(chatModel);

        ChatClient.ChatClientRequestSpec requestSpec = chatClient
                .prompt()
                .system(agent.getSystemPrompt())
                .user(request.getContent());

        if (!agent.getToolIds().isEmpty()) {
            requestSpec = requestSpec.tools(agent.getToolIds().stream()
                    .map(toolRegistry::get)
                    .toArray(Object[]::new));
        }

        String content = requestSpec
                .toolContext(Map.of(
                        "agentId", agent.getAgentId(),
                        "channelType", request.getChannelType(),
                        "channelId", request.getChannelId(),
                        "responseUrl", request.responseUrl()
                ))
                .call()
                .content();

        if (content == null) {
            throw new IllegalStateException(
                    "OpenRouter hat keine gültige Antwort geliefert"
            );
        }

        return new GenericResponseDto(
                request.getChannelType(),
                request.getChannelId(),
                content
        );
    }
}
