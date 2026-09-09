package com.example.telegramconnector.cli;

import com.example.telegramconnector.domain.TelegramChannel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/channels")
public class TelegramChannelAdminController {

    private final TelegramChannelService channelService;

    public TelegramChannelAdminController(
            TelegramChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    public List<TelegramChannel> listChannels() {
        return channelService.listChannels();
    }

    @PostMapping
    public ResponseEntity<TelegramChannel> addChannel(
            @RequestBody AddChannelRequest request) {

        TelegramChannel channel = channelService.addChannel(
                request.channelId(),
                request.name(),
                request.botToken()
        );

        return ResponseEntity.ok(channel);
    }

    public record AddChannelRequest(
            String channelId,
            String name,
            String botToken
    ) {}
}