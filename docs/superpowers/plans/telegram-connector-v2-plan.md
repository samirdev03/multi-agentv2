# Implementierungsplan – telegram-connector-v2

Quelle: vom Menschen bereitgestellter Implementierungsplan (Phasen 0–9). Dieser Datei bricht den
Plan in Tasks für Subagent-Driven Development herunter. Der Code je Task ist, wo möglich, wörtlich
aus dem Originalplan übernommen (inkl. deutscher Kommentare — das ist Absicht, nicht zu übersetzen).

Modul-Wurzel: `telegram-connector-v2/` (Maven-Modul unter dem Parent `org.example:multi-agent`).
Das Build-Grundgerüst (Maven-Wrapper, `pom.xml` mit allen Abhängigkeiten) ist bereits committet
(Commit "chore(telegram-connector-v2): scaffold Maven build for the connector"). Kein Task muss
`pom.xml` mehr anlegen — nur bei Bedarf ergänzen, falls eine Abhängigkeit fehlt (sollte nicht
vorkommen).

## Global Constraints (gelten für JEDEN Task)

- **Package-Wurzel:** `com.example.telegramconnector` (siehe Package-Struktur unten). Ein
  Alt-Placeholder unter `org.example` existiert nicht mehr — er wurde als Scaffold-Artefakt aus dem
  IntelliJ-Template entfernt.
- **Java:** 21 (`maven.compiler.release=21`, bereits in `pom.xml` gesetzt). Lauf-JDK lokal ist 25,
  das ist unkritisch, da mit `--release 21` kompiliert wird.
- **Abhängigkeiten** sind bereits in `telegram-connector-v2/pom.xml` gepinnt: Spring Boot
  `3.3.13` (als importierte BOM), Spring Shell `3.3.2`, `telegrambots-meta` `7.7.0`, Lombok
  (Version aus Spring-Boot-BOM), PostgreSQL-Treiber, Flyway (`flyway-core` +
  `flyway-database-postgresql`), H2 (Test-Scope), `reactor-test` (Test-Scope).
- **Tests laufen ausschließlich gegen H2, NICHT gegen echtes PostgreSQL.** In dieser Umgebung ist
  weder Docker noch ein lokaler Postgres-Server verfügbar, Testcontainers ist daher keine Option.
  Das entspricht ohnehin der Technologie-Tabelle des Originalplans ("H2 (nur Tests)"). Für
  `@DataJpaTest` reicht die Spring-Boot-Standard-Autokonfiguration (eingebettetes H2). Für jeden
  Test, der Flyway-Migrationen mitlaufen lässt, H2 im PostgreSQL-Kompatibilitätsmodus verwenden:
  JDBC-URL `jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1` in
  `src/test/resources/application.yml` (Profil `test`). Die V1-Migration (`VARCHAR`-Spalten,
  einfache `CREATE TABLE`) ist mit diesem Modus kompatibel.
- **Verifikation `mvn spring-boot:run` gegen echtes Postgres ist in dieser Umgebung nicht
  durchführbar** (kein lokaler Postgres). Wo der Originalplan das als DoD nennt, gilt ersatzweise:
  `./mvnw -q compile` (Modul kompiliert, Spring-Kontext lädt ohne fehlende Beans — durch
  `./mvnw test` mit einem schlanken Kontext-Smoke-Test abgedeckt, falls im Task erwähnt) plus
  `./mvnw test`. Immer aus dem Modul-Verzeichnis `telegram-connector-v2/` heraus mit
  `../mvnw -f pom.xml <goal>` oder aus dem Repo-Root mit `./mvnw -pl telegram-connector-v2 <goal>`
  aufrufen (Wrapper liegt im Repo-Root, nicht im Modul).
- **Kommentarsprache im Code: Deutsch, wörtlich wie im Originalplan.** Das weicht vom sonst
  gültigen CLAUDE.md-Standard (Englisch) ab — das ist für dieses Modul explizit vom Menschen so
  bestätigt. Nicht auf Englisch übersetzen.
- **Lombok:** `@Getter`/`@NoArgsConstructor` exakt wie im Plan-Code angegeben. Kein zusätzliches
  Lombok jenseits dessen, was der Plan-Code zeigt.
- **Keine eigenmächtigen Erweiterungen:** nur das implementieren, was der jeweilige Task-Text
  verlangt. Insbesondere keinen Rückweg (Telegram-seitiges Senden), kein Retry/Rate-Limiting für
  die Telegram Bot API — das ist laut Originalplan bewusst außerhalb des Scopes.

### Package-Struktur (Referenz)

```
com.example.telegramconnector
├── TelegramConnectorApplication.java
├── config
│   ├── TelegramConnectorProperties.java
│   └── WebClientConfig.java
├── domain
│   ├── TelegramChannel.java
│   └── TelegramMessage.java
├── repository
│   └── TelegramChannelRepository.java
├── service
│   ├── TelegramChannelResolver.java
│   ├── TelegramMessageForwardingService.java
│   └── exception
│       └── ChannelNotFoundException.java
├── client
│   ├── AgentRuntimeClient.java
│   └── TelegramBotRegistrationClient.java
├── web
│   ├── TelegramWebhookController.java
│   └── GlobalExceptionHandler.java
└── cli
    └── TelegramChannelCliCommands.java
```

---

## Task 1 — Bootstrap & Domain

Entspricht Phase 0 (Projekt-Grundgerüst) + Phase 1 (Domain) des Originalplans. Beide werden in
einem Task zusammengefasst, weil Phase 0 ohne mindestens eine Domain-Klasse kein sinnvoll
überprüfbares Ergebnis liefert und beide zusammen die kleinstmögliche kompilierbare Einheit bilden.

**Ziel:** Startfähige, leere Spring-Boot-Anwendung mit korrektem Dual-Mode (Webserver *oder*
CLI-Kommando), plus `TelegramChannel` (Entity) und `TelegramMessage` (DTO).

### Dateien

**`src/main/java/com/example/telegramconnector/TelegramConnectorApplication.java`**

```java
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
```

> Wichtig: `spring.shell.interactive.enabled=false` ist zwingend nötig, sonst fällt Spring Shell bei
> jedem Start ohne Argumente in eine interaktive REPL statt den Webhook-Server hochzufahren. Diese
> Property gehört in `application.yml` — das ist Teil von **Task 6** (Konfiguration), nicht dieses
> Tasks. Für diesen Task reicht es, dass `WebApplicationType.NONE` bei Argumenten gesetzt wird; ein
> minimales `src/main/resources/application.yml` mit nur `spring.application.name:
> telegram-connector` und `server.port: 8080` genügt hier, damit der Kontext lädt. Vollständige
> Konfiguration (Datasource, Flyway, Shell, telegram-connector-Properties) kommt in Task 6.

**`src/main/java/com/example/telegramconnector/config/TelegramConnectorProperties.java`**

```java
package com.example.telegramconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram-connector")
public record TelegramConnectorProperties(
        String agentRuntimeBaseUrl,
        String publicBaseUrl
) {
}
```

**`src/main/java/com/example/telegramconnector/config/WebClientConfig.java`**

```java
package com.example.telegramconnector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

**`src/main/java/com/example/telegramconnector/domain/TelegramChannel.java`**

```java
package com.example.telegramconnector.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "telegram_channel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nur fuer JPA, nicht fuer Anwendungscode
public class TelegramChannel {

    @Id
    @Column(name = "channel_id", nullable = false, updatable = false)
    private String channelId;

    @Column(nullable = false)
    private String name;

    /** Bot-Token von @BotFather. Wird für setWebhook() und künftige ausgehende Bot-API-Aufrufe benötigt. */
    @Column(name = "bot_token", nullable = false)
    private String botToken;

    public TelegramChannel(String channelId, String name, String botToken) {
        this.channelId = requireNonBlank(channelId, "channelId");
        this.name = requireNonBlank(name, "name");
        this.botToken = requireNonBlank(botToken, "botToken");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein");
        }
        return value;
    }
}
```

**`src/main/java/com/example/telegramconnector/domain/TelegramMessage.java`**

```java
package com.example.telegramconnector.domain;

public record TelegramMessage(String message, String channelId) {

    public TelegramMessage {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message darf nicht leer sein");
        }
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalArgumentException("channelId darf nicht leer sein");
        }
    }
}
```

> Als `record` statt als Klasse mit `getMessage()`/`getChannelId()` – die generierten
> Zugriffsmethoden heißen `message()` und `channelId()`.

### Tests (Teil dieses Tasks, nicht separat)

- Unit-Test für `TelegramChannel`: gültige Werte erzeugen ein Objekt mit den erwarteten Gettern;
  jeweils leerer/blank `channelId`, `name`, `botToken` wirft `IllegalArgumentException`.
- Unit-Test für `TelegramMessage`: gültige Werte erzeugen ein Record mit den erwarteten
  Zugriffsmethoden; leere/blank `message` bzw. `channelId` wirft `IllegalArgumentException`.

### Definition of Done

- `./mvnw -pl telegram-connector-v2 -am compile` (aus Repo-Root) kompiliert fehlerfrei.
- `./mvnw -pl telegram-connector-v2 -am test` läuft grün und deckt die oben genannten
  Validierungsfälle ab.
- Kein Datasource/Flyway/CLI-Code in diesem Task — das kommt in späteren Tasks.

---

## Task 2 — Persistenz

Entspricht Phase 2 des Originalplans.

**Ziel:** `TelegramChannel` per Spring Data JPA speichern/laden, versioniertes Schema via Flyway.

### Dateien

**`src/main/java/com/example/telegramconnector/repository/TelegramChannelRepository.java`**

```java
package com.example.telegramconnector.repository;

import com.example.telegramconnector.domain.TelegramChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChannelRepository extends JpaRepository<TelegramChannel, String> {
}
```

Keine eigene Methode nötig – `save()`, `findById()`, `findAll()`, `existsById()` aus
`JpaRepository` reichen für alles, was Resolver und CLI brauchen.

**`src/main/resources/db/migration/V1__init.sql`**

```sql
CREATE TABLE telegram_channel (
    channel_id VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    bot_token  VARCHAR(255) NOT NULL
);
```

**`src/test/resources/application.yml`** (Test-Profil, falls noch nicht vorhanden — für
`@DataJpaTest` und alle künftigen `@SpringBootTest`s in diesem Modul):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  flyway:
    enabled: true
  shell:
    interactive:
      enabled: false
```

### Tests

- `@DataJpaTest`: legt einen `TelegramChannel` per `repository.save(...)` an, lädt ihn per
  `findById(channelId)` wieder und prüft, dass alle Felder (channelId, name, botToken)
  übereinstimmen. Flyway muss beim Testkontext-Start die Migration `V1__init.sql` fehlerfrei gegen
  H2 (PostgreSQL-Modus) anwenden.

### Definition of Done

- `./mvnw -pl telegram-connector-v2 -am test` grün, inkl. des neuen `@DataJpaTest`.
- Flyway-Migration läuft beim Testkontext-Start ohne Fehler (sichtbar im Testlog).

---

## Task 3 — Channel-Auflösung & Fehlerbehandlung

Entspricht Phase 3 des Originalplans.

**Ziel:** Aus einer `channelId` den zugehörigen `TelegramChannel` auflösen; unbekannte Channels
sauber als HTTP 404 nach außen melden statt als 500.

### Dateien

**`src/main/java/com/example/telegramconnector/service/exception/ChannelNotFoundException.java`**

```java
package com.example.telegramconnector.service.exception;

public class ChannelNotFoundException extends RuntimeException {

    private final String channelId;

    public ChannelNotFoundException(String channelId) {
        super("Kein TelegramChannel mit channelId '" + channelId + "' registriert");
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }
}
```

**`src/main/java/com/example/telegramconnector/service/TelegramChannelResolver.java`**

```java
package com.example.telegramconnector.service;

import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import com.example.telegramconnector.service.exception.ChannelNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TelegramChannelResolver {

    private final TelegramChannelRepository channelRepository;

    public TelegramChannelResolver(TelegramChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public TelegramChannel resolveChannel(String channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));
    }
}
```

**`src/main/java/com/example/telegramconnector/web/GlobalExceptionHandler.java`**

```java
package com.example.telegramconnector.web;

import com.example.telegramconnector.service.exception.ChannelNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<String> handleChannelNotFound(ChannelNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
```

### Tests

- Unit-Test mit gemocktem `TelegramChannelRepository` (Mockito): bekannte `channelId` liefert den
  Channel zurück; unbekannte `channelId` wirft `ChannelNotFoundException`.
- Der Integrationstest für den 404-Pfad über den Controller ist Teil von **Task 4**, da der
  Controller dort erst entsteht — hier nicht vorwegnehmen.

### Definition of Done

- `./mvnw -pl telegram-connector-v2 -am test` grün, inkl. `TelegramChannelResolverTest`.

---

## Task 4 — Eingehender Webhook & Weiterleitung an agent-runtime

Entspricht Phase 4 (Webhook-Controller) + Phase 5 (Weiterleitung an agent-runtime) des
Originalplans. Zusammengefasst, weil der Controller aus Phase 4 im Originalplan bereits
`TelegramMessageForwardingService` referenziert, das erst in Phase 5 entsteht — als eigenständiger
Task wäre Phase 4 nicht kompilierbar.

**Ziel:** `POST /webhook/{channelId}` nimmt ein Telegram-`Update` entgegen, löst den Channel auf,
extrahiert Text (falls vorhanden), verpackt ihn in ein `TelegramMessage` und sendet es asynchron
per `WebClient` an agent-runtime.

### Dateien

**`src/main/java/com/example/telegramconnector/client/AgentRuntimeClient.java`**

```java
package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
import com.example.telegramconnector.domain.TelegramMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AgentRuntimeClient {

    private final WebClient webClient;

    public AgentRuntimeClient(WebClient.Builder webClientBuilder,
                               TelegramConnectorProperties properties) {
        this.webClient = webClientBuilder
                .baseUrl(properties.agentRuntimeBaseUrl())
                .build();
    }

    /**
     * Nicht-blockierender Aufruf an die asynchrone REST-API von agent-runtime.
     */
    public Mono<Void> sendAsync(TelegramMessage message) {
        return webClient.post()
                .uri("/api/messages")
                .bodyValue(message)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
```

**`src/main/java/com/example/telegramconnector/service/TelegramMessageForwardingService.java`**

```java
package com.example.telegramconnector.service;

import com.example.telegramconnector.client.AgentRuntimeClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.domain.TelegramMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelegramMessageForwardingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageForwardingService.class);

    private final AgentRuntimeClient agentRuntimeClient;

    public TelegramMessageForwardingService(AgentRuntimeClient agentRuntimeClient) {
        this.agentRuntimeClient = agentRuntimeClient;
    }

    public void forward(TelegramChannel channel, String rawText) {
        TelegramMessage message = new TelegramMessage(rawText, channel.getChannelId());
        agentRuntimeClient.sendAsync(message)
                .doOnError(error -> log.error(
                        "Weiterleitung an agent-runtime fehlgeschlagen fuer channelId={}",
                        channel.getChannelId(), error))
                .subscribe();
    }
}
```

**`src/main/java/com/example/telegramconnector/web/TelegramWebhookController.java`**

```java
package com.example.telegramconnector.web;

import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.service.TelegramChannelResolver;
import com.example.telegramconnector.service.TelegramMessageForwardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class TelegramWebhookController {

    private final TelegramChannelResolver channelResolver;
    private final TelegramMessageForwardingService forwardingService;

    public TelegramWebhookController(TelegramChannelResolver channelResolver,
                                      TelegramMessageForwardingService forwardingService) {
        this.channelResolver = channelResolver;
        this.forwardingService = forwardingService;
    }

    @PostMapping("/webhook/{channelId}")
    public ResponseEntity<Void> receiveUpdate(@PathVariable String channelId,
                                               @RequestBody Update update) {
        TelegramChannel channel = channelResolver.resolveChannel(channelId);

        String text = extractText(update);
        if (text != null) {
            forwardingService.forward(channel, text);
        }
        // Telegram erwartet zuegig eine 200er-Antwort, unabhaengig davon, ob Textinhalt
        // vorhanden war - sonst wiederholt Telegram den Zustellversuch.
        return ResponseEntity.ok().build();
    }

    private String extractText(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().getText();
        }
        return null;
    }
}
```

### Tests

- `TelegramMessageForwardingServiceTest`: Unit-Test mit gemocktem `AgentRuntimeClient` (liefert
  `Mono.empty()` für `sendAsync(...)`, z. B. via `reactor-test`/Mockito). `forward(channel, text)`
  ruft `sendAsync()` mit einem `TelegramMessage` auf, dessen Felder korrekt aus Channel und Rohtext
  zusammengesetzt sind (verify per `ArgumentCaptor`).
- `TelegramWebhookControllerTest` (`@WebMvcTest(TelegramWebhookController.class)`) mit gemocktem
  `TelegramChannelResolver` und `TelegramMessageForwardingService`:
  - Update mit Text (`message.text` gesetzt) → HTTP 200 und genau ein Aufruf von
    `forwardingService.forward(...)`.
  - Update ohne Message (z. B. nur `edited_message`, `hasMessage()` liefert `false`) → HTTP 200,
    aber **kein** Aufruf von `forward(...)`.
  - Unbekannte `channelId` (Resolver wirft `ChannelNotFoundException`) → HTTP 404. Dafür
    `GlobalExceptionHandler` aus Task 3 im `@WebMvcTest`-Slice mit importieren
    (`@Import(GlobalExceptionHandler.class)`), da `@WebMvcTest` `@RestControllerAdvice`-Beans aus
    anderen Packages nicht automatisch aufnimmt, wenn sie außerhalb des Standard-Scans für den
    Slice liegen — falls sie automatisch gefunden werden, ist der `@Import` redundant, aber
    unschädlich.

### Definition of Done

- `./mvnw -pl telegram-connector-v2 -am test` grün, inkl. beider neuer Testklassen.

---

## Task 5 — CLI zur Channel-Verwaltung & automatische Bot-Registrierung

Entspricht Phase 6 (CLI) + Phase 7 (automatische Bot-Registrierung) des Originalplans.
Zusammengefasst, weil `TelegramChannelCliCommands` aus Phase 6 direkt von
`TelegramBotRegistrationClient` aus Phase 7 abhängt.

**Ziel:** Neue Channels per Kommandozeile anlegen, inklusive automatischer Webhook-Registrierung bei
Telegram. Zusätzlich ein Lese-Kommando zur Kontrolle.

### Dateien

**`src/main/java/com/example/telegramconnector/client/TelegramBotRegistrationClient.java`**

```java
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
```

**`src/main/java/com/example/telegramconnector/cli/TelegramChannelCliCommands.java`**

```java
package com.example.telegramconnector.cli;

import com.example.telegramconnector.client.TelegramBotRegistrationClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class TelegramChannelCliCommands {

    private final TelegramChannelRepository channelRepository;
    private final TelegramBotRegistrationClient registrationClient;

    public TelegramChannelCliCommands(TelegramChannelRepository channelRepository,
                                       TelegramBotRegistrationClient registrationClient) {
        this.channelRepository = channelRepository;
        this.registrationClient = registrationClient;
    }

    @ShellMethod(key = "add-channel",
            value = "Legt einen neuen Telegram-Channel an und registriert den Webhook bei Telegram")
    public String addChannel(
            @ShellOption(help = "Eindeutige, frei waehlbare Channel-ID (Teil des Webhook-Pfads)") String channelId,
            @ShellOption(help = "Anzeigename des Bots") String name,
            @ShellOption(help = "Bot-Token von @BotFather") String botToken) {

        if (channelRepository.existsById(channelId)) {
            return "Channel '" + channelId + "' existiert bereits.";
        }

        TelegramChannel channel = new TelegramChannel(channelId, name, botToken);
        channelRepository.save(channel);
        registrationClient.registerWebhook(channelId, botToken);

        return "Channel '" + channelId + "' angelegt und Webhook bei Telegram registriert.";
    }

    @ShellMethod(key = "list-channels", value = "Listet alle konfigurierten Telegram-Channels auf")
    public String listChannels() {
        var channels = channelRepository.findAll();
        if (channels.isEmpty()) {
            return "Keine Channels konfiguriert.";
        }
        StringBuilder sb = new StringBuilder();
        for (TelegramChannel channel : channels) {
            sb.append(channel.getChannelId()).append(" - ").append(channel.getName())
              .append(System.lineSeparator());
        }
        return sb.toString();
    }
}
```

### Tests

- `TelegramChannelCliCommandsTest`: Unit-Test mit gemocktem `TelegramChannelRepository` und
  gemocktem `TelegramBotRegistrationClient`.
  - `addChannel(...)` mit neuer `channelId`: `repository.save(...)` wird mit einem
    `TelegramChannel` aufgerufen, dessen Felder den Argumenten entsprechen; danach wird
    `registrationClient.registerWebhook(channelId, botToken)` aufgerufen; Rückgabewert enthält
    "angelegt".
  - `addChannel(...)` mit bereits existierender `channelId` (`existsById` liefert `true`):
    **kein** Aufruf von `save(...)` oder `registerWebhook(...)`; Rückgabewert enthält "existiert
    bereits".
  - `listChannels()` bei leerem Repository liefert "Keine Channels konfiguriert."; bei gefülltem
    Repository liefert einen String, der `channelId` und `name` jedes Channels enthält.
- `TelegramBotRegistrationClientTest`: Da ein echter Telegram-Bot-Token in dieser Umgebung nicht
  verfügbar ist (DoD des Originalplans verlangt Verifikation via `getWebhookInfo` gegen einen
  echten Token — das ist hier nicht durchführbar), stattdessen: Test mit `MockWebServer`
  (`okhttp3.mockwebserver`, als Test-Dependency ergänzen falls nicht vorhanden — sonst
  `WebClient` gegen eine lokale Stub-Route testen) oder alternativ ein WireMock-Server, der die
  `WebClient`-`baseUrl` auf `http://localhost:<port>` umbiegt (Property-Override im Test statt
  `https://api.telegram.org`). Ziel des Tests: `registerWebhook(channelId, botToken)` ruft
  `GET /bot{token}/setWebhook?url=...` mit der aus `publicBaseUrl` und `channelId`
  zusammengesetzten Webhook-URL auf. Wenn du dich für einen dieser Test-Ansätze entscheidest,
  wähle den mit der geringsten zusätzlichen Abhängigkeit; wenn keiner sauber ohne zusätzliche
  Dependency umsetzbar ist, melde das als Concern statt eine schwere neue Abhängigkeit
  einzuführen, und beschränke dich auf den Unit-Test von `TelegramChannelCliCommands` (der
  `TelegramBotRegistrationClient` dort mockt) als Ersatzabdeckung.

### Definition of Done

- `./mvnw -pl telegram-connector-v2 -am test` grün.
- CLI-Verhalten ist über die Unit-Tests abgedeckt (echter End-to-End-Lauf `java -jar ... add-channel
  ...` gegen die echte Telegram-API ist in dieser Umgebung nicht verifizierbar und nicht Teil der
  DoD hier).

---

## Task 6 — Konfiguration & Deployment

Entspricht Phase 8 des Originalplans, angepasst an das Fehlen von Docker/Postgres in dieser
Umgebung (siehe Global Constraints).

**Ziel:** Vollständige `application.yml` (Produktionsprofil), `Dockerfile`, sodass das Modul
buildfähig und (bei Verfügbarkeit von Postgres/Traefik) deploybar ist.

### Dateien

**`src/main/resources/application.yml`** (ersetzt die minimale Version aus Task 1 vollständig):

```yaml
spring:
  application:
    name: telegram-connector
  datasource:
    url: jdbc:postgresql://localhost:5432/telegram_connector
    username: ${DB_USERNAME:telegram_connector}
    password: ${DB_PASSWORD:changeme}
  jpa:
    hibernate:
      ddl-auto: validate   # Schema kommt aus Flyway-Migrationen, nicht aus Hibernate-Auto-DDL
    open-in-view: false
  flyway:
    enabled: true
  shell:
    interactive:
      enabled: false       # verhindert REPL-Start bei normalem Boot ohne Argumente

server:
  port: 8080

telegram-connector:
  agent-runtime-base-url: http://agent-runtime:8080
  public-base-url: https://bots.deine-domain.de
```

**`Dockerfile`** (im Modul-Wurzelverzeichnis `telegram-connector-v2/Dockerfile`):

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/telegram-connector-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> Reverse Proxy / Traefik-Anbindung ist laut Originalplan bereits an anderer Stelle geklärt
> (`docker-compose.traefik.yml`, `telegram-connector-traefik-labels.yml`) und **nicht** Teil dieses
> Tasks — diese Dateien existieren in diesem Repository nicht und sollen hier nicht neu erfunden
> werden.

### Definition of Done

- `./mvnw -pl telegram-connector-v2 -am -DskipTests package` erzeugt
  `telegram-connector-v2/target/telegram-connector-<version>.jar` (Boot-Fat-Jar durch
  `spring-boot-maven-plugin`, bereits in `pom.xml` aus dem Scaffold-Commit konfiguriert).
- `docker build -t telegram-connector -f telegram-connector-v2/Dockerfile
  telegram-connector-v2` **kann in dieser Umgebung nicht ausgeführt werden** (kein Docker
  installiert) — das `Dockerfile` selbst muss aber syntaktisch korrekt sein und exakt dem
  Originalplan entsprechen. Als Ersatz-Verifikation: `target/telegram-connector-*.jar` existiert
  nach dem Package-Schritt und der Dateiname passt zum `COPY`-Pfad im Dockerfile.
- Alle bisherigen Tests (`./mvnw -pl telegram-connector-v2 -am test`) bleiben grün — insbesondere
  darf das Ersetzen von `application.yml` keinen bestehenden Test brechen (Datasource-URL wird in
  Tests durch `src/test/resources/application.yml` aus Task 2 überschrieben, prüfen dass das
  Overriding weiterhin greift).

---

## Task 7 — End-to-End-Integrationstest

Restlicher Teil von Phase 9 des Originalplans, der nicht bereits in Task 1–5 als Unit-/Slice-Test
abgedeckt ist: ein durchgängiger Test vom eingehenden Webhook-Request bis zum (gemockten)
`AgentRuntimeClient`-Aufruf, mit echtem Spring-Kontext (`@SpringBootTest`) statt gemockter Slices.

**Ziel:** Nachweisen, dass Webhook-Controller, Resolver, Persistenz (H2 statt der im Originalplan
vorgeschlagenen Testcontainers-Postgres-Instanz, siehe Global Constraints) und Forwarding-Service
im echten Spring-Kontext zusammenspielen.

### Test

`src/test/java/com/example/telegramconnector/TelegramWebhookIntegrationTest.java` (Name ist ein
Vorschlag, kein Zwang):

- `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` mit dem H2-Testprofil
  aus Task 2/6.
- `AgentRuntimeClient` wird **nicht** gemockt, sondern mit einer echten `WebClient`-Instanz gegen
  einen lokal gestarteten Stub (z. B. `MockWebServer` aus `okhttp3.mockwebserver`, falls in Task 5
  bereits als Test-Dependency ergänzt — sonst hier ergänzen) verifiziert, ODER `AgentRuntimeClient`
  wird per `@MockitoBean` (Spring Boot 3.3: `org.springframework.test.context.bean.override.mockito.MockitoBean`,
  Nachfolger von `@MockBean`) ersetzt und per `verify(...)` geprüft — wähle den Ansatz mit
  weniger zusätzlicher Infrastruktur; `@MockitoBean` ist hier vermutlich der pragmatischere Weg,
  da bereits Mockito über `spring-boot-starter-test` vorhanden ist.
- Ablauf: Über `TelegramChannelRepository` einen `TelegramChannel` anlegen → `POST
  /webhook/{channelId}` mit einem JSON-Body senden, der einem `Update` mit `message.text` entspricht
  → HTTP 200 erwarten → verifizieren, dass `AgentRuntimeClient.sendAsync(...)` (bzw. der
  Mock-Server) mit einem `TelegramMessage` aufgerufen wurde, dessen `message()` dem gesendeten Text
  und `channelId()` der verwendeten Channel-ID entspricht. Da `forward(...)` `subscribe()`
  aufruft (fire-and-forget), ggf. mit `Awaitility` oder einem kurzen `Thread.sleep`/`verify(...,
  timeout(...))` auf den asynchronen Aufruf warten statt sofort zu assertieren.
- Zweiter Fall: `POST /webhook/{unbekannte-channelId}` → HTTP 404, kein Aufruf des
  `AgentRuntimeClient`.

### Definition of Done

- `./mvnw -pl telegram-connector-v2 -am test` grün, inkl. dieses Integrationstests.
- Der Test ist deterministisch (kein flaky `Thread.sleep` ohne Timeout-basierte Absicherung).
