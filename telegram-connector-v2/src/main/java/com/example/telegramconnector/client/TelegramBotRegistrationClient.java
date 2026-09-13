package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
import com.example.telegramconnector.domain.TelegramChannel;
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
    public void registerWebhook(TelegramChannel channel) {
        String webhookUrl = publicBaseUrl + "/webhook/" + channel.getChannelId();
        String secretToken = channel.getSecretToken();

        String uri = "/bot{token}/setWebhook?url={url}";
        if (secretToken != null && !secretToken.isBlank()) {
            // secret_token enthaelt nur URL-sichere Zeichen (A-Z, a-z, 0-9, -, _)
            uri += "&secret_token=" + secretToken;
        }

        webClient.get()
                .uri(uri, channel.getBotToken(), webhookUrl)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}