package com.example.telegramconnector.api;

import com.example.telegramconnector.service.ResponseDeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * Gemeinsamer Endpoint fuer Vordergrund-Pfad (agent-runtime) und Hintergrund-Pfad
 * (tool-execution-service) - dieser Controller muss nicht wissen, welcher der beiden Aufrufer
 * die Antwort geliefert hat (invertierte Response-Kontrolle, siehe
 * agent-system-architecture.md Abschnitt 3).
 */
@RestController
public class ResponseDeliveryController {

    private final ResponseDeliveryService responseDeliveryService;

    public ResponseDeliveryController(ResponseDeliveryService responseDeliveryService) {
        this.responseDeliveryService = responseDeliveryService;
    }

    @PostMapping("/api/v1/responses")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deliverResponse(@RequestBody GenericResponseRequest request) {
        responseDeliveryService.deliver(request.channelId(), request.content(), request.attachments());
    }

    @PostMapping("/api/v1/responses/{channelId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deliverByStableChannelId(@PathVariable String channelId, @RequestBody GenericResponseRequest request) {
        responseDeliveryService.deliverStable(channelId, request.content(), request.attachments());
    }
}
