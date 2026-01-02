# NotifyHub Backend

NotifyHub is a production-style notification platform designed to demonstrate real-world backend patterns used in enterprise systems.

This project focuses on:
- Event-driven processing
- Reliability patterns (idempotency, retries, DLQs)
- Clean API design
- Cloud-native deployment on AWS
- Observability and operational readiness

This repository is a simplified, open implementation inspired by production systems I have worked on professionally, adapted for portfolio and learning purposes.

## Architecture Overview
The system is designed as a set of backend services:
- API service for accepting notification requests
- Worker service for asynchronous message delivery
- Messaging layer for decoupled processing

## Tech Stack
- Java 17
- Spring Boot 3
- PostgreSQL
- Docker
- AWS (EC2, S3, RDS, SQS – later stages)

## Status
🚧 Work in progress — initial API and database setup in progress.
