# Testing Strategy (v1)

NotifyHub uses two test layers:

## 1) Unit tests 
- Target: service/domain logic
- No Spring context
- Use Mockito + AssertJ

Examples:
- `InboundMessageServiceTest` verifies:
    - defaulting `receivedAt`
    - saving entity with `RECEIVED`
    - response fields

## 2) Integration tests 
- Target: HTTP endpoints + validation + persistence wiring
- Uses Spring Boot test slice:
    - `@SpringBootTest` + `@AutoConfigureMockMvc`

DB approach:
- v1: can use Testcontainers Postgres later
- for now: keep integration tests minimal and stable
