# NotifyHub Domain (v1)

## Purpose
NotifyHub centralizes inbound messages from multiple channels (SMS, WhatsApp, USSD) into a unified backend model for storage, async processing, and export/reporting.

> This doc defines the domain model and responsibilities. Provider-specific webhook details belong in `docs/integrations.md`.

---

## Core Concepts

### InboundMessage
A single message received from an external channel.

#### Fields (v1)
- id: UUID
- channel: enum (SMS | WHATSAPP | USSD)
- phoneNumber: string
- body: string
- receivedAt: timestamp (UTC)
- status: enum (RECEIVED | VALIDATED | QUEUED | PROCESSED | FAILED)
- createdAt: timestamp (UTC)
- updatedAt: timestamp (UTC)

#### Notes
- `phoneNumber` and `body` are immutable once stored (audit-friendly).
- Additional dynamic fields (custom form fields) are future scope (v2).

---

## Status Lifecycle (v1)

RECEIVED
  -> VALIDATED
  -> QUEUED
  -> PROCESSED
  -> FAILED (with retry policy later)

Rules:
- API creates message in RECEIVED.
- API performs basic validation and moves to VALIDATED.
- API publishes an async job/event and moves to QUEUED.
- Worker consumes job/event, processes, then marks PROCESSED or FAILED.

---

## Service Responsibilities

### notifyhub-api
- Expose REST endpoints to accept inbound messages (webhooks / normalized ingest)
- Validate and persist inbound message
- Publish async work item for processing
- Expose query/report endpoints (later)

### notifyhub-worker
- Consume async work items
- Execute processing steps (enrichment, routing, delivery, retries later)
- Update message status and processing metadata

---

## Scope

### v1 Scope
- Store inbound messages in PostgreSQL
- Basic validation
- Status lifecycle + async processing (simple worker)
- Health checks + migrations

### Later (v2+)
- Provider adapters (Twilio/Africa's Talking/etc.)
- Dynamic fields on messages
- Webhook-out / integrations
- Retry policies + DLQ
- Auth + admin users
- Metrics dashboards and tracing
