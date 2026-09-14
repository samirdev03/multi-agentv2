package org.example.callback;

import org.example.api.dto.GenericResponseDto;
import org.example.config.CallbackProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Objects;

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
                .anyMatch(allowedBaseUrl -> hasSameOrigin(allowedBaseUrl, responseUrl)
                        && hasAllowedPath(allowedBaseUrl, responseUrl));
    }

    private boolean hasSameOrigin(URI allowedBaseUrl, URI responseUrl) {
        return allowedBaseUrl.getHost() != null
                && responseUrl.getHost() != null
                && allowedBaseUrl.getScheme().equalsIgnoreCase(responseUrl.getScheme())
                && allowedBaseUrl.getHost().equalsIgnoreCase(responseUrl.getHost())
                && effectivePort(allowedBaseUrl) == effectivePort(responseUrl);
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }

        return switch (uri.getScheme().toLowerCase()) {
            case "http" -> 80;
            case "https" -> 443;
            default -> -1;
        };
    }

    private boolean hasAllowedPath(URI allowedBaseUrl, URI responseUrl) {
        String allowedPath = normalizedPath(allowedBaseUrl);
        if ("/".equals(allowedPath)) {
            return true;
        }

        String responsePath = normalizedPath(responseUrl);
        return responsePath.equals(allowedPath)
                || responsePath.startsWith(allowedPath + "/");
    }

    private String normalizedPath(URI uri) {
        String path = Objects.requireNonNullElse(uri.normalize().getRawPath(), "/");
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }
}
