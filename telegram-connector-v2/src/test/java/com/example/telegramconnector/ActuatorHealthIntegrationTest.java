package com.example.telegramconnector;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduziert den Produktionsfehler direkt: der Dockerfile-HEALTHCHECK
 * (telegram-connector-v2/Dockerfile) prueft per wget gegen /actuator/health und erwartet
 * "status":"UP" - ohne spring-boot-starter-actuator als Abhaengigkeit liefert dieser Pfad nur
 * ein generisches 404 vom normalen Spring-MVC-Dispatching, nicht vom Actuator. Dieser Test
 * faellt rot, solange die Abhaengigkeit fehlt, und schuetzt vor einer erneuten Regression
 * (Container war 18h dauerhaft unhealthy - siehe projects/Telegram-Agent-System/decisions.md
 * im Obsidian-Vault).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorHealthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void health_returnsOkWithStatusUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}
