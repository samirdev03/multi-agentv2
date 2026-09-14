# Generic Callback Contract Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Agent Runtime channel-agnostic while the Telegram Connector serializes the generic contract and delivers text and file attachments.

**Architecture:** The connector supplies a validated callback URL with a concrete generic request. The runtime processes that request, creates a `GenericResponseDto`, and posts it through a generic callback client. The connector owns all Telegram-specific request mapping, including multipart uploads.

**Tech Stack:** Java 21, Spring Boot MVC/WebFlux, Jackson, JUnit 5, Mockito, WebClient, RestClient.

## Global Constraints

- The wire fields are `channelType`, `channelId`, `content`, `responseUrl`, and `attachments`.
- The runtime contains no Telegram DTO or Telegram channel implementation.
- The callback URL must be restricted to configured trusted base URLs.
- `IMAGE` maps to Telegram `sendPhoto`; `PDF`, `TEXT`, and `FILE` map to `sendDocument`.

---

### Task 1: Generic runtime request and callback delivery

**Files:**
- Create: `agent-runtime/src/main/java/org/example/api/dto/GenericRequestDto.java`
- Create: `agent-runtime/src/main/java/org/example/callback/CallbackResponseClient.java`
- Create: `agent-runtime/src/main/java/org/example/config/CallbackProperties.java`
- Modify: `agent-runtime/src/main/java/org/example/api/controller/MessageReceiver.java`
- Modify: `agent-runtime/src/main/java/org/example/Service/RequestProcessingService.java`
- Modify: `agent-runtime/src/main/resources/application.properties`
- Test: `agent-runtime/src/test/java/org/example/api/controller/MessageReceiverTest.java`
- Test: `agent-runtime/src/test/java/org/example/callback/CallbackResponseClientTest.java`

- [ ] Write a failing MVC test that posts the generic JSON and verifies `GenericRequestDto` is accepted.
- [ ] Run `mvn -pl agent-runtime -Dtest=MessageReceiverTest test`; expect compilation/test failure because the concrete request DTO does not exist.
- [ ] Add `GenericRequestDto(ChannelType channelType, String channelId, String content, URI responseUrl)` implementing `RequestDto`; accept it in `MessageReceiver` and pass it to processing.
- [ ] Run the test; expect pass.
- [ ] Write a failing callback-client test for an allowed URL and a rejected URL.
- [ ] Run `mvn -pl agent-runtime -Dtest=CallbackResponseClientTest test`; expect failure because no callback client or allowlist exists.
- [ ] Implement `CallbackProperties` with `allowedBaseUrls`, validate that the callback URL starts with one configured base URL, then POST `GenericResponseDto` with `RestClient`.
- [ ] Refactor processing to construct `GenericResponseDto` and invoke the callback client; remove the runtime Telegram channel selection.
- [ ] Run both focused tests; expect pass.

### Task 2: Remove Runtime Telegram dependencies from the response path

**Files:**
- Delete: `agent-runtime/src/main/java/org/example/api/dto/TelegramRequestDto.java`
- Delete: `agent-runtime/src/main/java/org/example/api/dto/TelegramResponseDto.java`
- Delete: `agent-runtime/src/main/java/org/example/llm/client/channel/TelegramChannel.java`
- Delete: `agent-runtime/src/main/java/org/example/llm/client/channel/Channel.java`
- Delete: `agent-runtime/src/main/java/org/example/llm/client/channel/ChannelRegistry.java`
- Modify: `agent-runtime/src/main/java/org/example/tools/service/SendMessageService.java`
- Modify: `agent-runtime/src/main/java/org/example/llm/client/OpenRouterClient.java`
- Test: `agent-runtime/src/test/java/org/example/tools/service/SendMessageServiceTest.java`

- [ ] Write a failing tool-service test that passes `responseUrl` and verifies a generic response with an attachment is submitted through `CallbackResponseClient`.
- [ ] Run `mvn -pl agent-runtime -Dtest=SendMessageServiceTest test`; expect failure because the service still resolves a channel.
- [ ] Pass `responseUrl` through the OpenRouter tool context; replace `ChannelRegistry` usage in `SendMessageService` with `CallbackResponseClient`.
- [ ] Remove obsolete Telegram DTO/channel classes and update affected tests to generic DTO assertions.
- [ ] Run `mvn -pl agent-runtime test`; expect all runtime tests to pass.

### Task 3: Generic Telegram connector response DTO and dispatch

**Files:**
- Create: `telegram-connector-v2/src/main/java/com/example/telegramconnector/api/GenericResponseRequest.java`
- Create: `telegram-connector-v2/src/main/java/com/example/telegramconnector/api/FileAttachmentRequest.java`
- Create: `telegram-connector-v2/src/main/java/com/example/telegramconnector/api/FileType.java`
- Modify: `telegram-connector-v2/src/main/java/com/example/telegramconnector/api/ResponseDeliveryController.java`
- Modify: `telegram-connector-v2/src/main/java/com/example/telegramconnector/service/ResponseDeliveryService.java`
- Test: `telegram-connector-v2/src/test/java/com/example/telegramconnector/api/ResponseDeliveryControllerTest.java`
- Test: `telegram-connector-v2/src/test/java/com/example/telegramconnector/service/ResponseDeliveryServiceTest.java`

- [ ] Write failing controller tests for `content` and a `PDF` attachment in the generic JSON body.
- [ ] Run `mvn -pl telegram-connector-v2 -Dtest=ResponseDeliveryControllerTest test`; expect failure because the connector expects `message` only.
- [ ] Introduce connector-local generic request records and delegate text plus attachment list to the delivery service.
- [ ] Write a failing delivery-service test that routes an image to `sendPhoto` and a PDF to `sendDocument`.
- [ ] Run `mvn -pl telegram-connector-v2 -Dtest=ResponseDeliveryServiceTest test`; expect failure because no attachment delivery exists.
- [ ] Implement ordered attachment delivery with the existing retry policy.
- [ ] Run both focused tests; expect pass.

### Task 4: Telegram multipart uploads and outgoing generic request

**Files:**
- Modify: `telegram-connector-v2/src/main/java/com/example/telegramconnector/client/AgentRuntimeClient.java`
- Modify: `telegram-connector-v2/src/main/java/com/example/telegramconnector/client/TelegramBotClient.java`
- Modify: `telegram-connector-v2/src/main/java/com/example/telegramconnector/config/TelegramConnectorProperties.java`
- Test: `telegram-connector-v2/src/test/java/com/example/telegramconnector/client/AgentRuntimeClientTest.java`
- Test: `telegram-connector-v2/src/test/java/com/example/telegramconnector/client/TelegramBotClientTest.java`

- [ ] Write a failing client test asserting the outgoing runtime JSON contains `content` and `${publicBaseUrl}/api/v1/responses` as `responseUrl`.
- [ ] Run `mvn -pl telegram-connector-v2 -Dtest=AgentRuntimeClientTest test`; expect failure because it sends `message` and lacks `responseUrl`.
- [ ] Make the outgoing request use the generic contract.
- [ ] Write failing multipart tests asserting `sendPhoto` for `IMAGE` and `sendDocument` plus `document` resource for `PDF`.
- [ ] Run `mvn -pl telegram-connector-v2 -Dtest=TelegramBotClientTest test`; expect failure because only JSON `sendMessage` exists.
- [ ] Implement multipart `sendPhoto` and `sendDocument`, sending captions when nonblank and file content as `FileSystemResource`.
- [ ] Run `mvn -pl telegram-connector-v2 test` and `mvn -pl agent-runtime test`; expect both module suites to pass.
