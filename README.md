# DeskBooking

REST API for booking desks in an office (hot-desking). Employees can see which desks are free on a given day and book one, admins manage offices and desks.

![CI](https://github.com/kamshik28/DeskBooking/actions/workflows/ci.yml/badge.svg)

## Tech stack

- Java 17, Spring Boot 3
- Spring Security + JWT
- Spring Data JPA, PostgreSQL, Flyway
- Swagger UI (springdoc-openapi)
- JUnit 5, Mockito, MockMvc, H2 for tests
- Docker, docker-compose, GitHub Actions

## Features

- registration and login, JWT authentication
- roles `USER` and `ADMIN`
- CRUD for offices and desks (admin only)
- search desks with filters and pagination
- list of free desks in an office for a selected day
- booking a desk for a day, cancelling a booking
- admin view of all bookings for a day

### Booking rules

- a desk can be booked only once per day
- a user can have only one booking per day
- no booking in the past, max 30 days ahead
- inactive desks can't be booked
- only the owner (or admin) can cancel a booking

Rules are checked in the service layer and also in the database with partial unique indexes, so two parallel requests can't book the same desk (the second one gets `409 Conflict`).

## Running

### With Docker

```bash
docker compose up --build
```

API runs on http://localhost:8080, Swagger UI on http://localhost:8080/swagger-ui.html

### Locally

You need Java 17+ and Maven. The easiest way to get the database is to start only the `db` container (it is exposed on port `5433` so it doesn't clash with a local PostgreSQL):

```bash
docker compose up -d db
mvn spring-boot:run
```

Database connection is configured with env variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (see `application.yml` and `.env.example`). Flyway creates the tables and adds demo offices and desks on first start.

Default admin account (change it with `ADMIN_EMAIL` / `ADMIN_PASSWORD`):

```
admin@deskbooking.local / admin123
```

### Tests

```bash
mvn test
```

## API

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| GET | `/api/auth/me` | user |
| GET | `/api/offices`, `/api/offices/{id}` | user |
| POST / PUT / DELETE | `/api/offices`, `/api/offices/{id}` | admin |
| GET | `/api/desks?officeId=&floor=&name=&page=&size=` | user |
| GET | `/api/desks/available?officeId=1&date=2026-10-20` | user |
| POST / PUT / DELETE | `/api/desks`, `/api/desks/{id}` | admin |
| POST | `/api/bookings` | user |
| GET | `/api/bookings/my` | user |
| PATCH | `/api/bookings/{id}/cancel` | owner / admin |
| GET | `/api/bookings?date=&officeId=` | admin |

Example requests are in [`requests.http`](requests.http) (can be run directly from IntelliJ).

Errors have the same format everywhere:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Desk is already booked for 2026-10-20",
  "path": "/api/bookings",
  "timestamp": "2026-10-10T12:00:00"
}
```

## Project structure

```
src/main/java/com/kaminskyi/deskbooking
├── config        security, swagger, app properties
├── controller    REST controllers
├── dto           request / response records
├── entity        JPA entities
├── exception     custom exceptions + global handler
├── repository    Spring Data repositories
├── security      JWT filter and service
└── service       business logic
```

## Possible improvements

- booking by time slots, not only whole days
- email notifications
- simple frontend
