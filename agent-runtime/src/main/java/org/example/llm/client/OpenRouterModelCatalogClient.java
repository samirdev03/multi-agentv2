package org.example.llm.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.Credential;
import org.example.config.CredentialRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.StreamSupport;

/** Loads and ranks OpenRouter's current free text models with tool support. */
@Component
public class OpenRouterModelCatalogClient {
    private static final Logger log = LoggerFactory.getLogger(OpenRouterModelCatalogClient.class);
    private final RestClient.Builder restClientBuilder;
    private final CredentialRegistry credentials;
    private final ObjectMapper objectMapper;

    public OpenRouterModelCatalogClient(RestClient.Builder restClientBuilder,
                                        CredentialRegistry credentials,
                                        ObjectMapper objectMapper) {
        this.restClientBuilder = restClientBuilder;
        this.credentials = credentials;
        this.objectMapper = objectMapper;
    }

    public Optional<String> findBestFreeTextToolCallingModel() {
        Credential credential = credentials.getRequired("openrouter");
        String baseUrl = credential.baseUrl().replaceFirst("/+$", "");
        try {
            String body = restClientBuilder.clone().baseUrl(baseUrl).build()
                    .get().uri("/models?output_modalities=text&input_modalities=text")
                    .headers(headers -> headers.setBearerAuth(credential.apiKey()))
                    .retrieve().body(String.class);
            JsonNode models = objectMapper.readTree(body).path("data");
            return StreamSupport.stream(models.spliterator(), false)
                    .filter(this::isFreeTextToolModel)
                    .sorted(Comparator.comparingLong(this::contextLength).reversed()
                            .thenComparing(node -> node.path("id").asText()))
                    .map(node -> node.path("id").asText())
                    .filter(id -> !id.isBlank())
                    .findFirst();
        } catch (Exception exception) {
            log.warn("OpenRouter model catalog unavailable; using configured fallback model", exception);
            return Optional.empty();
        }
    }

    private boolean isFreeTextToolModel(JsonNode model) {
        JsonNode pricing = model.path("pricing");
        boolean free = isZero(pricing.path("prompt").asText()) && isZero(pricing.path("completion").asText());
        boolean text = contains(model.path("architecture").path("input_modalities"), "text")
                && contains(model.path("architecture").path("output_modalities"), "text");
        JsonNode supported = model.path("supported_parameters");
        boolean tools = contains(supported, "tools") || contains(supported, "tool_choice");
        return free && text && tools;
    }

    private boolean contains(JsonNode values, String expected) {
        if (!values.isArray()) {
            return false;
        }
        for (JsonNode value : values) {
            if (expected.equals(value.asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean isZero(String value) {
        try { return new BigDecimal(value).compareTo(BigDecimal.ZERO) == 0; }
        catch (NumberFormatException ignored) { return false; }
    }

    private long contextLength(JsonNode model) { return model.path("context_length").asLong(0); }
}
