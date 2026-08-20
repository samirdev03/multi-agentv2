package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kein echter Telegram-Bot-Token in dieser Umgebung verfuegbar, daher kein Test gegen die echte
 * Telegram-API. Stattdessen wird die tatsaechlich aufgebaute HTTP-Anfrage abgefangen, indem dem
 * WebClient.Builder eine Stub-ExchangeFunction untergeschoben wird (Teil von spring-webflux,
 * bereits Projektabhaengigkeit - keine zusaetzliche Test-Dependency noetig). Die
 * ExchangeFunction ersetzt die eigentliche Netzwerk-Transportschicht, sodass die volle
 * aufgeloeste URL (inkl. der fest verdrahteten TELEGRAM_API_BASE) inspiziert werden kann, ohne
 * einen echten Server zu starten oder DNS/Netzwerk anzufassen.
 */
class TelegramBotRegistrationClientTest {

    @Test
    void registerWebhook_callsSetWebhookWithBotTokenAndComposedWebhookUrl() {
        // Given
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        WebClient.Builder stubbedBuilder = WebClient.builder()
                .exchangeFunction(request -> {
                    capturedRequest.set(request);
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                });
        TelegramConnectorProperties properties =
                new TelegramConnectorProperties("http://agent-runtime.internal", "https://public.example.com");
        TelegramBotRegistrationClient client = new TelegramBotRegistrationClient(stubbedBuilder, properties);

        String channelId = "test-channel-123";
        String botToken = "bot-token-123";

        // When
        client.registerWebhook(channelId, botToken);

        // Then
        ClientRequest request = capturedRequest.get();
        assertThat(request).isNotNull();
        assertThat(request.method()).isEqualTo(HttpMethod.GET);

        UriComponents uri = UriComponentsBuilder.fromUri(request.url()).build();
        assertThat(uri.getScheme()).isEqualTo("https");
        assertThat(uri.getHost()).isEqualTo("api.telegram.org");
        assertThat(uri.getPath()).isEqualTo("/bot" + botToken + "/setWebhook");

        String rawUrlParam = uri.getQueryParams().getFirst("url");
        assertThat(rawUrlParam).isNotNull();
        String decodedUrlParam = URLDecoder.decode(rawUrlParam, StandardCharsets.UTF_8);
        assertThat(decodedUrlParam).isEqualTo("https://public.example.com/webhook/" + channelId);
    }
}
