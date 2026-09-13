package org.example.llm.client;

import lombok.RequiredArgsConstructor;
import org.example.agent.AgentEntity;
import org.example.config.Credential;
import org.example.config.CredentialRegistry;
import org.example.llm.dto.ResponseDto;
import org.example.tools.DateTimeTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenRouterClient implements Client {

    private final CredentialRegistry credentialRegistry;
    private final DateTimeTools dateTimeTools;

    @Override
    public ResponseDto getResponse(
            AgentEntity agent,
            String userMessage
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

        String content = chatClient
                .prompt()
                .system(agent.getSystemPrompt())
                .user(userMessage)

                // Hier bekommt der Agent seine Tools
                .tools(dateTimeTools)

                .call()
                .content();

        if (content == null) {
            throw new IllegalStateException(
                    "OpenRouter hat keine gültige Antwort geliefert"
            );
        }

        return new ResponseDto(content);
    }
}