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

## Register a user

```bash
curl -i http://localhost:8080/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "victor@example.com",
    "password": "correct-horse-battery-staple",
    "displayName": "Victor"
  }'
```

The endpoint normalizes email addresses, hashes passwords with Argon2id, and
creates the user plus its password identity in one database transaction.

Model API keys, conversations, memories, attachments, and workspace paths remain local to cc-agents Desktop and must never be persisted by this service.
