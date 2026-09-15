package org.example.callback;

import com.sun.net.httpserver.HttpServer;
import org.example.web.dto.ChannelType;
import org.example.web.dto.GenericResponseDto;
import org.example.config.CallbackProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallbackResponseClientTest {

    private HttpServer server;
    private URI serverBaseUrl;
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> connectorToken = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/v1/responses", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes()));
            connectorToken.set(exchange.getRequestHeaders().getFirst("X-Connector-Token"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        serverBaseUrl = URI.create("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void sendResponse_postsGenericResponseToAllowedCallbackUrl() {
        CallbackResponseClient client = new CallbackResponseClient(
                RestClient.builder(),
                new CallbackProperties(List.of(serverBaseUrl), "callback-token")
        );

        client.sendResponse(
                serverBaseUrl.resolve("/api/v1/responses"),
                new GenericResponseDto(ChannelType.TELEGRAM, "12345", "Hello connector")
        );

        assertThat(requestBody.get()).contains("\"channelType\":\"TELEGRAM\"");
        assertThat(requestBody.get()).contains("\"channelId\":\"12345\"");
        assertThat(requestBody.get()).contains("\"content\":\"Hello connector\"");
        assertThat(connectorToken.get()).isEqualTo("callback-token");
    }

    @Test
    void sendResponse_rejectsCallbackUrlOutsideAllowedBaseUrls() {
        CallbackResponseClient client = new CallbackResponseClient(
                RestClient.builder(),
                new CallbackProperties(List.of(serverBaseUrl), "callback-token")
        );

        URI untrustedUrl = URI.create(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/api/v1/responses"
        );

        assertThatThrownBy(() -> client.sendResponse(
                untrustedUrl,
                new GenericResponseDto(ChannelType.TELEGRAM, "12345", "Hello connector")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
        assertThat(requestBody.get()).isNull();
    }

    @Test
    void sendResponse_rejectsCallbackUrlWithAnAllowedHostPrefix() {
        CallbackResponseClient client = new CallbackResponseClient(
                RestClient.builder(),
                new CallbackProperties(List.of(URI.create("http://connector:8080")), "callback-token")
        );

        URI untrustedUrl = URI.create(
                "http://connector:8080.evil.example/api/v1/responses"
        );

        assertThat(isAllowed(client, untrustedUrl)).isFalse();
    }

    private boolean isAllowed(CallbackResponseClient client, URI responseUrl) {
        try {
            Method isAllowed = CallbackResponseClient.class
                    .getDeclaredMethod("isAllowed", URI.class);
            isAllowed.setAccessible(true);
            return (boolean) isAllowed.invoke(client, responseUrl);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
