# NotifyHub Architecture (WIP)

NotifyHub is designed as two deployable services:
- notifyhub-api: accepts requests and enqueues work
- notifyhub-worker: processes queued work and updates delivery status

Messaging layer: AWS SQS (Phase 2)
Database: PostgreSQL
