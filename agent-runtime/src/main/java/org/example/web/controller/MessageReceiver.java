package org.example.web.controller;

import lombok.RequiredArgsConstructor;
import org.example.Service.RequestProcessingService;
import org.example.web.dto.GenericRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageReceiver {
    private final RequestProcessingService processingService;
    @PostMapping
    public ResponseEntity<Void> receiveMessage(@RequestBody GenericRequestDto request) {
        processingService.process(request);
        return ResponseEntity.accepted().build();
    }

}
