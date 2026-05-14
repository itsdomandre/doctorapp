# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DoctorApp is a medical appointment management system for aesthetic procedures (botox, fillers, laser therapy, etc.). It provides patient registration with email verification, appointment booking with slot generation based on business hours, and admin approval workflows.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.3.5, Spring Security + JWT, Spring Data JPA, PostgreSQL 16
- **Frontend**: React 18 + TypeScript + Vite (port 5173)
- **Email**: Mailhog (dev), smtp.titan.email (prod)
- **Testing**: JUnit 5, Mockito, REST Assured, Playwright

## Commands

### Build & Run

```bash
# Start dependencies (PostgreSQL + Mailhog)
docker compose -f docker/docker-compose.yml up -d

# Run backend (dev profile)
mvn spring-boot:run

# Build JAR (skip tests)
mvn clean package -DskipTests
```

### Testing

```bash
# Unit/service tests only
mvn test -Dtest="*ServiceTest"

# API integration tests (requires backend on :8080)
mvn test -Dtest=AuthenticationApiTest

# UI/E2E tests (requires backend :8080 + frontend :5173)
mvn test -Dtest="*UiTest"

# Run all tests
mvn test
```

Mailhog UI for inspecting emails in dev: http://localhost:8081

## Architecture

### Layered Structure

```
controllers/ → services/ → repositories/ → PostgreSQL
     ↕              ↕
request/response  entities/
   DTOs          mappers/
```

Controllers use dedicated request/response DTO classes; mappers handle entity↔DTO conversion. Services contain all business logic.

### Authentication Flow

Stateless JWT with HS512 signing. The `JwtAuthenticationFilter` validates tokens on every request. Users start as `UNVERIFIED` (Role: USER) until confirming their email via `ConfirmationToken`. An `InvalidToken` table serves as a JWT blacklist for logout.

Access tokens are set as cookies (dev mode also exposes them in response bodies for testing). Password resets use separate short-lived `PasswordResetToken` entities.

### Appointment Slot Logic

Business hours are encoded in `BusinessHoursHelper` — not all weekdays have the same hours, and Saturday is closed. Appointment slots are 1-hour intervals generated from these hours. The `AppointmentValidator` enforces slot availability, and `AppointmentStatus` progresses: `REQUESTED → APPROVED/REJECTED → COMPLETED/CANCELLED`.

### Profiles

| Profile | DB URL | Notes |
|---------|--------|-------|
| `dev` (default) | `localhost:5432` | tokens exposed in responses |
| `prod` | `172.17.0.1:5432` | Docker internal network, file logging |

### Test Base Classes

- `BaseApiTest` — configures REST Assured against `http://localhost:8080`
- `BaseUiTest` — launches Playwright; reads `BE_URL` and `FE_URL` system properties (defaults to localhost)

### Key Enums

- `Procedures` — 11 aesthetic procedure types (the allowed appointment kinds)
- `Role` — `USER`, `ADMIN` (admin approves/rejects appointments)
- `AppointmentStatus` — `REQUESTED`, `APPROVED`, `REJECTED`, `CANCELLED`, `COMPLETED`
- `UserStatus` — `ACTIVE`, `UNVERIFIED`

## Exception Handling

Global exception handler maps 11 custom exception types (under `exceptions/`) to HTTP responses. Add new exceptions there rather than returning error strings from controllers.

## Development Notes

- The frontend proxy IP is `192.168.1.9` — update `vite.config.ts` if the machine changes
- Default admin credentials for testing: `deise@example.com` / `Password123!`
- Default user credentials for testing: `andre@example.com` / `Password123!`

## Do NOT

- Do not modify `BusinessHoursHelper` without updating the slot-related tests
- Do not return error strings from controllers — always use the custom exceptions under `exceptions/`
- Do not create new endpoints without a dedicated request/response DTO pair
