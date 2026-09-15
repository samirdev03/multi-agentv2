# HTTP and Incoming File Tools Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a safe HTTP content tool and a Telegram-to-runtime file ingestion/read pipeline while preserving the existing `tools` and `tools/service` structure, then enable both tools for Agent 3 and deploy.

**Architecture:** Telegram connector downloads incoming documents through the Bot API, stores them in its persistent data directory, and forwards file metadata/content to agent-runtime. Agent-runtime persists incoming files under its configured sandbox and exposes a `FileReadTool`; `HttpTool` fetches allowlisted web content with bounded size/time. Tool definitions remain in `org.example.tools`, services in `org.example.tools.service`.

**Tech Stack:** Java 21, Spring Boot 3, Spring Data JPA, WebClient/RestClient, Apache PDFBox, Apache POI, Apache Tika, JUnit 5.

## Global Constraints

- Keep existing package layout and tool registry conventions.
- Reject SSRF targets, unsupported MIME types, oversized payloads, and paths outside the file sandbox.
- Preserve existing Telegram stable-channel response behavior.

### Task 1: Runtime HTTP and file-reading tools

**Files:**
- Create `agent-runtime/src/main/java/org/example/tools/HttpTool.java`
- Create `agent-runtime/src/main/java/org/example/tools/FileReadTool.java`
- Create services under `agent-runtime/src/main/java/org/example/tools/service/`
- Modify `agent-runtime/src/main/java/org/example/tools/ToolRegistry.java`
- Add focused unit tests under `agent-runtime/src/test/java/org/example/tools/`

- [ ] Write failing tests for allowlisted HTTP fetches, SSRF rejection, sandbox path validation, and PDF/TXT/CSV/JSON/DOCX/XLSX extraction.
- [ ] Implement bounded HTTP and file extraction services.
- [ ] Register both tools and run runtime tests.

### Task 2: Telegram incoming file ingestion

**Files:**
- Modify webhook DTO/service/controller classes under `telegram-connector-v2/src/main/java/com/example/telegramconnector/`
- Create persistent incoming-file entity/repository and migration.
- Modify `AgentRuntimeClient` to forward file metadata/content.
- Add integration/unit tests.

- [ ] Test Telegram document metadata forwarding and download failure handling.
- [ ] Implement Bot API `getFile`/download, persistent storage, and runtime forwarding.
- [ ] Run the full connector suite.

### Task 3: Runtime persistence and Agent 3 configuration

**Files:**
- Create incoming-file entity/repository/service in `agent-runtime`.
- Modify Agent 3 tool assignment/configuration.
- Add migration and end-to-end tests.

- [ ] Persist received files and expose stable file IDs to the agent context.
- [ ] Enable HTTP and file-read tool IDs for Agent 3.
- [ ] Verify tool execution and run all module tests.

### Task 4: Deploy and verify

- [ ] Commit and push to `master`.
- [ ] Monitor GitHub Actions deploy run.
- [ ] Verify both containers are healthy and perform a production smoke test.
