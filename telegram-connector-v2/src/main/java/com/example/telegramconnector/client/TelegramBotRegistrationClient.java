package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TelegramBotRegistrationClient {

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org";

    private final WebClient webClient;
    private final String publicBaseUrl;

    public TelegramBotRegistrationClient(WebClient.Builder webClientBuilder,
                                          TelegramConnectorProperties properties) {
        this.webClient = webClientBuilder.baseUrl(TELEGRAM_API_BASE).build();
        this.publicBaseUrl = properties.publicBaseUrl();
    }

    /**
     * Registriert den Webhook fuer einen Bot bei Telegram. Bewusst blockierend (.block()):
     * dieser Aufruf passiert ausschliesslich innerhalb der CLI (ein kurzlebiger Prozess,
     * kein Request-Pfad des laufenden Webservers), dort ist Blockieren unproblematisch und
     * deutlich einfacher als eine reaktive CLI-Kommandokette.
     */
    public void registerWebhook(String channelId, String botToken) {
        String webhookUrl = publicBaseUrl + "/webhook/" + channelId;

        webClient.get()
                .uri("/bot{token}/setWebhook?url={url}", botToken, webhookUrl)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
