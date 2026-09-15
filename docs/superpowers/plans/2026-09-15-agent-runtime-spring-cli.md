# Agent Runtime Spring CLI Implementation Plan

**Goal:** Provide a Spring Shell CLI mode for persistent JPA management of agents and their channels.

**Architecture:** CLI commands call a transactional management service backed by `AgentRepository`. Agent creation always persists one channel; updates modify agent fields and that channel atomically. Registry overview commands expose configured provider codes, enum channel types, and registered tool IDs.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Shell 3.3.2, Spring Data JPA, JUnit 5.

## Scope

- Add Spring Shell dependency and a `cli` profile that disables the web server.
- Add repository query for duplicate channel ownership.
- Add transactional management service with create/list/show/update/delete operations.
- Add shell commands for management and registry overviews.
- Add registry listing methods and unit tests.
