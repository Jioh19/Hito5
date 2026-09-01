# Hito4 — Identity Microservice

A productive, persistent Spring Boot microservice for user identity management: registration, login, lookup, listing, and deletion. Evolves the Hito1 CLI domain/application core (Clean Architecture / Ports & Adapters) by swapping the CLI adapter for a REST controller adapter and the in-memory/file repository adapters for a JPA + PostgreSQL adapter, all behind the same `IIdentityRepository` port.

## Tech Stack

Java 21, Spring Boot 4.1.1 (Web MVC, Spring Data JPA), PostgreSQL 16, Docker Compose, springdoc-openapi (Swagger UI).

## Architecture

- **Domain** (`domain.entity`, `domain.valueobject`, `domain.exception`, `domain.service`, `domain.repository`) — framework-agnostic business rules, unchanged from Hito1.
- **Application** (`application.usecase`, `application.dto`) — one use case per business flow, Spring `@Service` beans injected with the `IIdentityRepository` port.
- **Infrastructure**
  - `infrastructure.persistence` — `UserEntity` (JPA mapping), `UserJpaRepository` (Spring Data JPA), `JpaIdentityRepository` (adapter implementing the domain's `IIdentityRepository` port over PostgreSQL).
  - `infrastructure.web` — `UserController` (REST adapter), `GlobalExceptionHandler` (`@RestControllerAdvice`, translates every domain exception into a semantic HTTP status + `ErrorResponse` body — no endpoint ever returns a raw stack trace).

## Running the Database

```bash
docker compose up -d
```

Starts PostgreSQL 16 on `localhost:5432` (database `tunegocio_db`), with a named volume so data survives restarts.

## Running the Application (Development Mode)

```bash
./mvnw spring-boot:run
```

Runs under the `dev` profile (default), which points at the local Dockerized database and enables Swagger UI.

## API Documentation and Contract Testing

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

Both are disabled when the `prod` profile is active (`SPRING_PROFILES_ACTIVE=prod`), returning 404 to eliminate the documentation attack surface outside local development.

## API Reference

| Method | Path | Description | Success | Error(s) |
|---|---|---|---|---|
| POST | `/api/v1/users` | Register a user | 201 | 400, 422 |
| POST | `/api/v1/users/login` | Authenticate | 200 | 404 |
| GET | `/api/v1/users` | List users | 200 | — |
| GET | `/api/v1/users/{id}` | Get a user by id | 200 | 404 |
| DELETE | `/api/v1/users/{id}` | Delete a user | 204 | 404 |

Every error response is a JSON `ErrorResponse`: `{ "message": "...", "code": "...", "timestamp": "..." }`.

## Running the Tests

```bash
docker compose up -d
./mvnw test
```

## Test Collection

No Bruno/Postman collection is included (optional per the rubric). Exercise the API manually with `curl`, e.g.:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jioh","password":"pass123","email":"jioh@example.com"}'
```
