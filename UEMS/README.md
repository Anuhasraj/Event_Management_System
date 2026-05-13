# UEMS Backend

University Event Management System backend built with Spring Boot, Spring Security JWT, Spring Data JPA, and MySQL.

## Run in IntelliJ IDEA

1. Create or start MySQL on `localhost:3306`.
2. Update `src/main/resources/application.properties` if your MySQL username/password are different.
3. Open this folder in IntelliJ IDEA.
4. Run `UemsApplication`.

The app runs on `http://localhost:8080`. Hibernate creates/updates the `uems_db` schema automatically.

Default admin account:

```text
username: admin
password: admin123
```

## Main API Endpoints

Public:

```text
POST /api/auth/register
POST /api/auth/login
```

Organizer:

```text
GET  /api/venues
GET  /api/events/my
GET  /api/events/venue/{venueId}/schedule?date=2026-06-10
POST /api/events/request
```

Admin:

```text
GET /api/admin/users
GET /api/events/admin/pending
GET /api/events/admin/all
PUT /api/events/admin/approve/{eventId}
PUT /api/events/admin/reject/{eventId}
```

Use the JWT from login/register in protected requests:

```text
Authorization: Bearer <token>
```

## Sample Event Request JSON

```json
{
  "eventName": "Tech Talk",
  "eventDate": "2026-06-10",
  "startTime": "09:00",
  "endTime": "11:00",
  "venueId": 2,
  "description": "Monthly technology session"
}
```

Venue double-booking is blocked when an existing `PENDING` or `APPROVED` event overlaps the same venue, date, and time.

## Run React Frontend

Open a terminal in the `frontend` folder:

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

Run the backend first on `http://localhost:8080`, then open the frontend URL in your browser.
