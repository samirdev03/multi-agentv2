package org.example.callback;

import org.example.api.dto.GenericResponseDto;
import org.example.config.CallbackProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
public class CallbackResponseClient {

    private final RestClient restClient;
    private final CallbackProperties callbackProperties;

    public CallbackResponseClient(
            RestClient.Builder restClientBuilder,
            CallbackProperties callbackProperties
    ) {
        this.restClient = restClientBuilder.build();
        this.callbackProperties = callbackProperties;
    }

    public void sendResponse(URI responseUrl, GenericResponseDto response) {
        if (!isAllowed(responseUrl)) {
            throw new IllegalArgumentException("Callback URL is not allowed: " + responseUrl);
        }

        restClient.post()
                .uri(responseUrl)
                .body(response)
                .retrieve()
                .toBodilessEntity();
    }

    private boolean isAllowed(URI responseUrl) {
        return callbackProperties.getAllowedBaseUrls().stream()
                .anyMatch(allowedBaseUrl -> responseUrl.toString()
                        .startsWith(allowedBaseUrl.toString()));
    }
}
