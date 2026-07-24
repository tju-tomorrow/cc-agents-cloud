# cc-agents-cloud

Cloud account and settings sync service for cc-agents Desktop.

## Stack

- Java 21
- Spring Boot 3.5
- PostgreSQL 17
- Flyway
- Gradle

## Local development

Start PostgreSQL:

```bash
docker compose up -d
```

The checked-in defaults are only for local development. To override them, copy
`.env.example` to `.env` and export the same variables before starting Spring.

Run the service:

```bash
./gradlew bootRun
```

Verify it:

```bash
curl http://localhost:8080/actuator/health
```

Run tests:

```bash
./gradlew test
```

Model API keys, conversations, memories, attachments, and workspace paths remain local to cc-agents Desktop and must never be persisted by this service.
