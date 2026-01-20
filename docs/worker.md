# Worker v1 (Processing Loop)

The worker is responsible for asynchronous processing and status transitions after ingestion.

## Current behavior (v1)
- Polls for `RECEIVED` messages in batches (ordered by `received_at`)
- Moves message to `PROCESSING`
- Executes placeholder processing (no external calls yet)
- Moves message to `PROCESSED`

## Why SQL (JdbcTemplate) for now
- Avoid duplicating JPA entities across services
- Keep the worker small and focused
- Still production-realistic: workers often use SQL directly for queues/outbox-like patterns

## Future (v2+)
- Replace polling with event-driven delivery (SQS/Kafka)
- Add retry/backoff + DLQ
- Add idempotency keys (provider_message_id)
