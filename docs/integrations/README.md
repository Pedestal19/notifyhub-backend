# Provider Integrations

This directory documents how external messaging providers integrate with NotifyHub.

NotifyHub is designed to be **provider-agnostic**.
All inbound provider payloads are mapped into a **canonical ingestion DTO**
before entering the core domain.

## Supported Integration Model

Providers deliver inbound messages to NotifyHub via HTTP webhooks.

Each provider:
- owns phone numbers / sender IDs
- receives messages from end users
- POSTs inbound payloads to NotifyHub

NotifyHub does not depend on provider-specific payloads internally.

## Planned Providers (Reference Only)

The following providers are planned or commonly supported in similar systems:

- Twilio (SMS / WhatsApp)
- Africa’s Talking (SMS / USSD)
- Termii (SMS)
- Infobip (SMS / WhatsApp)
- Meta WhatsApp Cloud API

These are **examples**, not commitments.

## Canonical Mapping Strategy

Each provider payload will be:
1. Verified (signature / auth – future)
2. Mapped into a canonical request DTO
3. Ingested via the same internal service

This ensures:
- one source of truth for ingestion rules
- consistent validation and persistence
- minimal coupling to provider formats

## Provider-Specific Docs (Future)

Provider-specific mappings will live here when implemented:

- `twilio.md`
- `africas-talking.md`
- `meta-whatsapp.md`

These documents will describe:
- webhook payload structure
- signature verification
- mapping rules to canonical DTO
