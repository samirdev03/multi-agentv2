package com.example.telegramconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TelegramConnectorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TelegramConnectorApplication.class);
        if (args.length > 0) {
            // Aufruf mit Argumenten = CLI-Kommando (z. B. add-channel). Dafuer wird kein
            // Embedded-Webserver benoetigt, der Prozess soll nach Ausfuehrung beenden.
            app.setWebApplicationType(WebApplicationType.NONE);
        }
        app.run(args);
    }
}
