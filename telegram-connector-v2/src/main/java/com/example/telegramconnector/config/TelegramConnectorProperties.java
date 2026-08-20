package com.example.telegramconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram-connector")
public record TelegramConnectorProperties(
        String agentRuntimeBaseUrl,
        String publicBaseUrl
) {
}
