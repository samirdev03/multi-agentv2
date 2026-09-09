package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.Service.RequestProcessingService;
import org.example.api.dto.TelegramMessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageReceiver {
    private RequestProcessingService processingService;
    @PostMapping
    public ResponseEntity<TelegramMessageDto> receiveMessage(@RequestBody TelegramMessageDto message){
        processingService.getTelegramResponse(message);
        return ResponseEntity.ok().build();
    }

}
