# NotifyHub Backend

NotifyHub is a production-style notification platform designed to demonstrate real-world backend patterns used in enterprise systems.

This project focuses on:
- Event-driven processing
- Reliability patterns (idempotency, retries, DLQs)
- Clean API design
- Cloud-native deployment on AWS
- Observability and operational readiness

This repository is a simplified, open implementation inspired by production systems I have worked on professionally, adapted for portfolio and learning purposes.

## What Problem Does NotifyHub Solve?

Organizations often receive messages from multiple channels such as SMS, USSD, and WhatsApp.
These messages are usually fragmented across different systems, making it difficult to track,
process, and report on them consistently.

NotifyHub centralizes incoming messages into a single backend platform where they can be:
- Stored reliably
- Processed asynchronously
- Enriched with metadata
- Forwarded to other systems
- Queried for reporting and analytics

## Design Principles

- Explicit data ownership

- Asynchronous processing where possible

- Clear separation between API and background work

- Operational visibility over hidden magic

## Intended Users

- **Administrators**
    - Manage system configuration
    - Monitor message throughput and failures
    - Export reports

- **Internal Systems / Integrations**
    - Submit messages via APIs
    - Receive processed messages via webhooks or queues
  
## Architecture Overview
The system is designed as a set of backend services:
- API service for accepting incoming messages from external channels
- Worker service for asynchronous processing, retries, and delivery
- Messaging layer for decoupled processing

## Database Migrations (Flyway)

NotifyHub uses Flyway for managing database schema migrations.

All database changes are versioned and applied automatically at application startup. This ensures that schema changes are consistent, repeatable, and safe across environments.

Migration files are located at:

```notifyhub-api/src/main/resources/db/migration```

Each migration follows Flyway’s versioned naming convention, for example:

```V1__create_inbound_message.sql```

In local development, Hibernate is configured with:

```ddl-auto: validate```

This means Hibernate validates the schema created by Flyway but does not attempt to create or modify tables itself. This approach mirrors production-grade systems where schema changes are controlled explicitly via migrations.

## Tech Stack
- Java 17
- Spring Boot 3
- PostgreSQL
- Docker
- AWS (EC2, RDS, SQS – planned for later stages)

## Schema Management Strategy

Flyway: owns schema creation and evolution

JPA/Hibernate: maps entities to an existing schema

No auto-DDL in runtime environments

This separation ensures:

- Clear ownership of database structure

- Safer deployments

- Easier debugging and rollbacks

## Status
🚧 Work in progress — core backend foundation and local infrastructure setup in progress.

Current focus areas:

- Database schema and migrations

- Domain modeling

- API foundations

- Observability via Actuator

## Local Development Setup
### Prerequisites

- **Docker** & **Docker Compose**
- **Java 17+**
- **Maven**
- **PostgreSQL client (`psql`)** – optional but recommended

### 🐘 PostgreSQL (Docker)

NotifyHub uses **PostgreSQL running in Docker** for local development.

#### Port Mapping

| Environment      | Port |
|------------------|------|
| Docker container | 5432 |
| Host machine     | 5433 |

> Port `5433` is used on the host to avoid conflicts with local PostgreSQL installations.

#### Start PostgreSQL

```docker compose up -d ```


Verify database connectivity:

```psql "postgres://notifyhub:notifyhub@127.0.0.1:5433/notifyhub" -c "select 1;"```


Expected output:

```1```

On first startup, Flyway will automatically create required tables and indexes.

If schema validation fails, check migration scripts before adjusting entity mappings.


#### Database Schema Management

Database schema is managed using Flyway migrations.

On application startup:

Flyway checks the flyway_schema_history table

Validates existing migrations

Applies new migrations if present

Fails fast if migrations are inconsistent

This ensures database integrity and predictable startup behavior.

### Running the Services

#### API

```cd notifyhub-api```

```mvn spring-boot:run```


Health check:

```http://localhost:8080/actuator/health```


Worker

```cd notifyhub-worker```

```mvn spring-boot:run```


Health check:

```http://localhost:8081/actuator/health```

## Notes

If you have PostgreSQL installed locally (e.g. Postgres.app), it usually runs on port 5432.

Docker PostgreSQL is intentionally mapped to 5433 to avoid conflicts.

Update application-dev.yml if you change ports.