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

## Tech Stack
- Java 17
- Spring Boot 3
- PostgreSQL
- Docker
- AWS (EC2, S3, RDS, SQS – later stages)

## Status
🚧 Work in progress — core backend foundation and local infrastructure setup in progress.

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