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

### Test DB isolation (important)

Integration tests must not share the same database used for manual local testing.
Otherwise, leftover rows can break ordering assertions (e.g. "hello" is newer than test data).

We support two modes:

**Mode A (fastest): Use docker-compose Postgres**
- Pros: 2 minutes setup, matches local env
- Cons: must clean tables before each test

Rule: integration tests must TRUNCATE/DELETE relevant tables in @BeforeEach.

**Mode B (most correct): Use Testcontainers**
- Pros: each test run gets an isolated Postgres container
- Cons: requires Docker environment to be detectable from the IDE

If Testcontainers fails in IntelliJ but works in Maven, confirm:
- Docker Desktop running
- IntelliJ is allowed to access Docker
- Tests run with the same JDK as Maven (Java 17)

