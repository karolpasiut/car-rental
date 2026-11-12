# DriveLocal — local car rental MVP

Stack: **Spring Boot** (Java 17) + **React/TypeScript** + **PostgreSQL**.

## Requirements

- Java 17+
- Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop (for Postgres)

## 1. PostgreSQL

From the project directory:

```powershell
docker compose up -d
```

Default connection:

| Parameter | Value |
|-----------|-------|
| URL | `jdbc:postgresql://localhost:5432/carrental` |
| User | `carrental` |
| Password | `carrental` |

Schema: `src/main/resources/schema.sql`.
The fleet seed also runs automatically on the first backend startup (`DataSeeder`).

Environment variables (optional):

- `CAR_RENTAL_DB_URL`
- `CAR_RENTAL_DB_USER`
- `CAR_RENTAL_DB_PASSWORD`

## 2. Backend

```powershell
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

### Endpoints (`ReservationBookingController`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/cars` | Car list; optional `?startDate=&endDate=` → type availability for the whole range |
| GET | `/reservation/{id}` | Reservation details |
| POST | `/bookReservation` | Book a quantity of cars by type |
| POST/DELETE | `/cancelReservation/{id}` | Cancel a reservation |
| GET | `/reservations/{startDate}/{endDate}` | Reservations in the date range |
| POST | `/checkAvailability` | Whether the requested types are available on every day |

Example body:

```json
{
  "startDate": "2026-11-20",
  "endDate": "2026-11-22",
  "cars": [
    { "type": "SUV", "quantity": 1 },
    { "type": "SEDAN", "quantity": 1 }
  ]
}
```

Types: `SUV`, `VAN`, `SEDAN`.

Tests:

```powershell
mvn test
```

## 3. Frontend

```powershell
cd frontend
npm install
npm run dev
```

App: `http://localhost:5173` (the Vite proxy forwards API calls to port 8080).

## Structure

```
car-rental/
  docker-compose.yml
  pom.xml
  src/main/java/com/carrental/          # Spring Boot
  src/main/resources/                   # schema, seed, application.yml
  frontend/                             # React + TypeScript (Vite)
```
