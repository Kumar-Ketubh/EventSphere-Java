# EventSphere MVP v1 — Execution Flow Documentation

## System Overview

EventSphere is a decoupled full-stack application:
- **Backend:** Spring Boot 3.x REST API (port `8080`)
- **Frontend:** React SPA via Vite (port `5173`)
- **Database:** H2 In-Memory (console at `/h2-console`)

---

## Entry Points

| Layer     | Entry Point                                            | Role                                      |
|-----------|--------------------------------------------------------|-------------------------------------------|
| Backend   | `EventSphereApplication.java`                          | Bootstraps Spring Boot context, seeds DB  |
| Frontend  | `main.jsx` → `App.jsx`                                 | Mounts React root, sets up Router & routes|

---

## 1. Application Bootstrap Flow

```
JVM Start
  └── EventSphereApplication.main()
        └── SpringApplication.run()
              ├── Spring Security Config loaded (SecurityConfig.java)
              ├── JPA entities scanned & H2 schema auto-created
              ├── CORS policy registered (WebConfig.java) → allow http://localhost:5173
              └── DataInitializer.run()
                    ├── Seeds 1 sample user: student@college.edu / password123
                    └── Seeds 5 events across Live / Upcoming / Previous timelines

Browser opens http://localhost:5173
  └── main.jsx renders <App />
        └── App.jsx sets up <BrowserRouter>
              ├── / → Login.jsx
              ├── /register → Register.jsx
              ├── /dashboard → Dashboard.jsx (protected)
              └── /profile → Profile.jsx (protected)
```

---

## 2. Authentication Flow — Login & Auth State Storage

### Step-by-step Control Path

```
[User] types email + password → clicks "Login"
  │
  ▼
Login.jsx (handleSubmit)
  └── api.js: login(email, password)
        └── POST http://localhost:8080/api/auth/login
              │  Body: { email, password }
              │
              ▼
        AuthController.login()
          └── AuthService.login(LoginRequest)
                └── UserRepository.findByEmail(email)
                      └── H2: SELECT * FROM users WHERE email = ?
                ├── [NOT FOUND]  → throws UserNotFoundException → 404
                ├── [WRONG PASS] → throws BadCredentialsException → 401
                └── [SUCCESS]    → returns UserResponse { id, name, email, rollNumber }
              │
              ▼
        HTTP 200 OK: { id, name, email, rollNumber }
              │
              ▼
Login.jsx receives response
  └── localStorage.setItem("user", JSON.stringify(userData))
        └── navigate("/dashboard")
```

**Auth State Storage Pattern:**
- User object is stored in `localStorage` under key `"user"`.
- `App.jsx` reads this on mount to determine protected route access.
- Logout clears `localStorage` and redirects to `/`.

---

## 3. Dashboard Fetch — Live / Upcoming / Previous Event Cards

### Step-by-step Control Path

```
[User] lands on /dashboard
  │
  ▼
Dashboard.jsx (useEffect on mount)
  └── api.js: getDashboard()
        └── GET http://localhost:8080/api/events/dashboard
              │  (no body, no auth header required for MVP)
              │
              ▼
        EventController.getDashboard()
          └── EventService.getDashboardData()
                │
                ├── EventRepository.findLiveEvents(now, now)
                │     └── JPQL: WHERE startTime <= :now AND endTime >= :now
                │           └── H2 query → List<Event> liveEvents
                │
                ├── EventRepository.findUpcomingEvents(now)
                │     └── JPQL: WHERE startTime > :now
                │           └── H2 query → List<Event> upcomingEvents
                │
                └── EventRepository.findPreviousEvents(now)
                      └── JPQL: WHERE endTime < :now
                            └── H2 query → List<Event> previousEvents
                │
                ▼
          Returns: DashboardResponse { live: [...], upcoming: [...], previous: [...] }
              │
              ▼
        HTTP 200 OK: { live, upcoming, previous }
              │
              ▼
Dashboard.jsx renders 3 sections:
  ├── Live Events   → green "LIVE" badge, seats shown, no register button
  ├── Upcoming      → blue badge, seats available counter, "Register Seat" button
  └── Previous      → grey "ENDED" badge, disabled state
```

**Dynamic Categorization Logic (Java):**
```java
LocalDateTime now = LocalDateTime.now();
live     → startTime <= now AND endTime >= now
upcoming → startTime > now
previous → endTime   < now
```

---

## 4. Seat Booking — POST `/api/events/{id}/register` with Pessimistic Locking

### Step-by-step Control Path

```
[User] clicks "Register Seat" on an Upcoming event card
  │
  ▼
Dashboard.jsx (handleRegister(eventId))
  └── api.js: registerForEvent(eventId, userId)
        └── POST http://localhost:8080/api/events/{eventId}/register
              │  Body: { userId }
              │
              ▼
        RegistrationController.register(eventId, body)
          └── RegistrationService.registerUserForEvent(userId, eventId)
                │
                ├── [STEP 1] Verify user exists
                │     UserRepository.findById(userId) → or throw UserNotFoundException
                │
                ├── [STEP 2] Fetch event with PESSIMISTIC WRITE lock
                │     EventRepository.findByIdWithLock(eventId)
                │       └── @Lock(PESSIMISTIC_WRITE) → SELECT ... FOR UPDATE
                │             └── Acquires DB row-level lock on Event row
                │                   (concurrent requests BLOCK here until lock released)
                │
                ├── [STEP 3] Duplicate check
                │     RegistrationRepository.existsByUserIdAndEventId(userId, eventId)
                │       └── [EXISTS] → throws AlreadyRegisteredException → 409
                │
                ├── [STEP 4] Seat availability check
                │     event.getBookedSeats() >= event.getTotalSeats()
                │       └── [FULL] → throws NoSeatsAvailableException → 400
                │
                ├── [STEP 5] Atomic seat increment + save
                │     event.setBookedSeats(event.getBookedSeats() + 1)
                │     EventRepository.save(event)       ← updates row, releases lock
                │
                └── [STEP 6] Create Registration record
                      Registration reg = new Registration(user, event, LocalDateTime.now())
                      RegistrationRepository.save(reg)
                │
                ▼
          @Transactional boundary ends → DB commit → lock released
              │
              ▼
        HTTP 200 OK: { message: "Registration successful", registrationId: ... }
              │
              ▼
Dashboard.jsx shows success toast / updates seat count in UI
```

**Why Pessimistic Locking?**
- Without locking, two concurrent `POST /register` requests could both read `bookedSeats = 49`, both pass the capacity check, and both write `50` — resulting in overbooking.
- `SELECT ... FOR UPDATE` serializes access at the DB row level, guaranteeing correctness.

---

## 5. Profile Rendering — GET `/api/profile`

### Step-by-step Control Path

```
[User] clicks "Profile" in Navbar
  │
  ▼
Profile.jsx (useEffect on mount)
  └── api.js: getProfile(userId)
        └── GET http://localhost:8080/api/profile?userId={userId}
              │
              ▼
        ProfileController.getProfile(userId)
          └── ProfileService.getProfileData(userId)
                ├── UserRepository.findById(userId) → UserResponse { name, email, rollNumber }
                └── RegistrationRepository.findByUserId(userId)
                      └── H2: SELECT r.*, e.* FROM registrations r JOIN events e ...
                            └── List<Registration> → mapped to List<RegistrationResponse>
                │
                ▼
          Returns: ProfileResponse { user: {...}, registrations: [...] }
              │
              ▼
        HTTP 200 OK: { user, registrations }
              │
              ▼
Profile.jsx renders:
  ├── User Card: name, email, roll number
  └── Registrations Table: event title, venue, startTime, registeredAt
```

---

## Data Flow Summary Diagram

```
React SPA (port 5173)
    │
    │  HTTP/JSON (Axios)
    ▼
Spring Boot REST API (port 8080)
    ├── AuthController       → AuthService       → UserRepository
    ├── EventController      → EventService      → EventRepository
    ├── RegistrationController → RegistrationService → RegistrationRepository + EventRepository (LOCKED)
    └── ProfileController    → ProfileService    → UserRepository + RegistrationRepository
                                                              │
                                                              ▼
                                                    H2 In-Memory Database
                                                    (JPA / Hibernate ORM)
```
