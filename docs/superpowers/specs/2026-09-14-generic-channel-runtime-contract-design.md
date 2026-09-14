# Generischer Connector-Vertrag für Agent Runtime

## Ziel

Die Agent Runtime verarbeitet Nachrichten und Antworten unabhängig vom verwendeten
Kommunikationskanal. Telegram-spezifische JSON-DTOs, Channel-Implementierungen und
HTTP-Aufrufe verbleiben ausschließlich im Telegram Connector.

## Transportvertrag

Der Endpoint `POST /api/v1/messages` akzeptiert ein konkretes generisches
`RequestDto` mit den Feldern `channelType`, `channelId`, `content` und `responseUrl`.

Die Runtime sendet nach asynchroner Verarbeitung ein konkretes generisches
`ResponseDto` an die im Request übermittelte `responseUrl`. Dieses DTO enthält
`channelType`, `channelId`, `content` und `attachments`. Jeder Anhang enthält
`path`, `fileName` und `type` (`PDF`, `TEXT`, `IMAGE`, `FILE`).

Der Vertrag verwendet keine Java-Interface-Deserialisierung und keine
channel-spezifischen DTOs. Dadurch können weitere Connectoren denselben Endpoint
verwenden. Die Runtime prüft die Callback-Adresse gegen eine konfigurierbare
Allowlist vertrauenswürdiger Connector-Basis-URLs.

## Verarbeitung

1. Der Telegram Connector wandelt einen Telegram-Text in das generische Request-DTO
   und setzt seine `/api/v1/responses`-Adresse als `responseUrl`.
2. Die Runtime verarbeitet das DTO und erzeugt ein generisches Response-DTO.
3. Die Runtime validiert `responseUrl` und POSTet dieses DTO an den Response-Endpoint
   des aufrufenden Connectors.
4. Der Telegram Connector liefert `content` als Text aus und verarbeitet die
   Anhänge nacheinander: `IMAGE` mit Telegram `sendPhoto`, alle übrigen Typen mit
   `sendDocument`.

## Fehlerbehandlung

Die bestehenden Connector-Retries bleiben für Runtime-Aufrufe und Telegram-API-Aufrufe
erhalten. Ungültige oder nicht existierende Anhangpfade werden vor dem Upload klar
abgewiesen. Eine leere Anhangliste ist zulässig.

## Tests

- JSON-Serialisierung des generischen Connector-Requests und -Responses.
- Deserialisierung des generischen Request-DTO durch die Runtime.
- Lieferung von Antworttext und Anhängen im Connector.
- Multipart-Aufrufe: Bilder gehen an `sendPhoto`, PDF/Text/generische Dateien an
  `sendDocument`.
