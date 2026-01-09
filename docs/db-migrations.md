# Database migrations (Flyway)

This project uses Flyway for database schema management.

- All schema changes are versioned SQL files
- Migrations run automatically on startup
- Hibernate is configured in validation-only mode

We manage schema changes using Flyway migrations.

## How it works
- Migration scripts live in: `notifyhub-api/src/main/resources/db/migration`
- Files are versioned: `V1__create_inbound_message.sql`, `V2__...`

## Running migrations
Migrations run automatically on application startup when Flyway is enabled.

## Local setup
- Postgres is started via Docker Compose
- App connects via `localhost:5433` (to avoid local Postgres conflicts)

## Conventions
- Prefer additive changes (new columns/tables) over destructive changes
- Each migration should be small and focused
