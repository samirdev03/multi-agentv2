package org.example.llm.client;

import lombok.RequiredArgsConstructor;
import org.example.agent.AgentEntity;
import org.example.config.Credential;
import org.example.config.CredentialRegistry;
import org.example.llm.dto.ResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenRouterClient implements Client{

    private final CredentialRegistry credentialRegistry;

    public ResponseDto getResponse(
            AgentEntity agent,
            String userMessage
    ) {

        Credential credential =
                credentialRegistry.getRequired("openrouter");

        RestClient restClient = RestClient.builder()
                .baseUrl(credential.baseUrl())
                .defaultHeader(
                        "Authorization",
                        "Bearer " + credential.apiKey()
                )
                .build();

        OpenRouterRequest request = new OpenRouterRequest(
                agent.getModelId(),
                List.of(
                        new OpenRouterMessage(
                                "user",
                                userMessage
                        )
                )
        );

        OpenRouterApiResponse response = restClient
                .post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(OpenRouterApiResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().getFirst().message() == null) {

            throw new IllegalStateException(
                    "OpenRouter hat keine gültige Antwort geliefert"
            );
        }

        String content =
                response.choices()
                        .getFirst()
                        .message()
                        .content();

        return new ResponseDto(content);
    }


    private record OpenRouterRequest(
            String model,
            List<OpenRouterMessage> messages
    ) {
    }


    private record OpenRouterMessage(
            String role,
            String content
    ) {
    }


    private record OpenRouterApiResponse(
            List<Choice> choices
    ) {
    }


    private record Choice(
            OpenRouterMessage message
    ) {
    }
}