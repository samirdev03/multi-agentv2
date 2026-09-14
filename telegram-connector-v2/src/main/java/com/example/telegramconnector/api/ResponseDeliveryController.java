package com.example.telegramconnector.api;

import com.example.telegramconnector.service.ResponseDeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Gemeinsamer Endpoint fuer Vordergrund-Pfad (agent-runtime) und Hintergrund-Pfad
 * (tool-execution-service) - dieser Controller muss nicht wissen, welcher der beiden Aufrufer
 * die Antwort geliefert hat (invertierte Response-Kontrolle, siehe
 * agent-system-architecture.md Abschnitt 3).
 */
@RestController
public class ResponseDeliveryController {

    private final ResponseDeliveryService responseDeliveryService;
    private final byte[] expectedCallbackToken;

    public ResponseDeliveryController(
            ResponseDeliveryService responseDeliveryService,
            @Value("${telegram-connector.callback-token}") String callbackToken
    ) {
        this.responseDeliveryService = responseDeliveryService;
        if (callbackToken == null || callbackToken.isBlank()) {
            throw new IllegalArgumentException("telegram-connector.callback-token must not be blank");
        }
        this.expectedCallbackToken = callbackToken.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/api/v1/responses")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deliverResponse(
            @RequestHeader(value = "X-Connector-Token", required = false) String callbackToken,
            @RequestBody GenericResponseRequest request
    ) {
        if (!hasValidCallbackToken(callbackToken)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        responseDeliveryService.deliver(request.channelId(), request.content(), request.attachments());
    }

    private boolean hasValidCallbackToken(String callbackToken) {
        return callbackToken != null && MessageDigest.isEqual(
                expectedCallbackToken,
                callbackToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
