# Generischer Connector-Vertrag für Agent Runtime

## Ziel

Die Agent Runtime verarbeitet Nachrichten und Antworten unabhängig vom verwendeten
Kommunikationskanal. Telegram-spezifische JSON-DTOs, Channel-Implementierungen und
HTTP-Aufrufe verbleiben ausschließlich im Telegram Connector.

## Transportvertrag

Der Endpoint `POST /api/v1/messages` akzeptiert ein konkretes generisches
`RequestDto` mit den Feldern `channelType`, `channelId` und `content`.

Die Runtime sendet nach asynchroner Verarbeitung ein konkretes generisches
`ResponseDto` an den konfigurierten Response-Endpoint. Dieses DTO enthält
`channelType`, `channelId`, `content` und `attachments`. Jeder Anhang enthält
`path`, `fileName` und `type` (`PDF`, `TEXT`, `IMAGE`, `FILE`).

Der Vertrag verwendet keine Java-Interface-Deserialisierung und keine
channel-spezifischen DTOs. Dadurch können weitere Connectoren denselben Endpoint
verwenden.

## Verarbeitung

1. Der Telegram Connector wandelt einen Telegram-Text in das generische Request-DTO.
2. Die Runtime verarbeitet das DTO und erzeugt ein generisches Response-DTO.
3. Die Runtime POSTet dieses DTO an den bereits bestehenden Response-Endpoint des
   Connectors.
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
