# EventSphere request flows

This file follows each user action from the React page to the database and back.

## Overview
- The backend is a Spring Boot 3 REST API on port 8080 (or dynamically assigned by cloud providers via `PORT`).
- The frontend is a React app built with Vite on port 5173 (served via Vite dev server in development or bundled statically with Spring Boot in production).
- The database is accessed via pure Spring JDBC (`JdbcTemplate`), defaulting to in-memory H2 with support for PostgreSQL.

| Part | Starts in | What it does |
|---|---|---|
| Backend | `EventSphereApplication.java` | Starts Spring Boot, initializes schema via `schema.sql`, and seeds initial test data |
| Frontend | `main.jsx`, then `App.jsx` | Renders the React app and sets up the page routes |

## 1. Startup

```
Backend starts: EventSphereApplication.main()
  SecurityConfig.java     login and sign-up are public, API calls need a token, static/SPA routes permitted
  schema.sql              pure ANSI SQL creates the tables (users, events, registrations)
  WebConfig.java          configures CORS for local dev and production
  DataInitializer.run()   checks userRepository.count() and seeds 1 student account and 5 events via JDBC

Browser opens http://localhost:5173
  main.jsx renders <App />
  App.jsx routes:
    /           Landing.jsx
    /login      Login.jsx
    /register   Register.jsx
    /dashboard  Dashboard.jsx  (needs a saved token)
    /profile    Profile.jsx    (needs a saved token)
    /tickets/:id Ticket.jsx    (needs a saved token)
```

## 2. Login

```
User enters email and password, clicks Login
Login.jsx calls loginUser(email, password) in api.js
  POST /api/auth/login   body: { email, password }
    AuthController.login()
      AuthServiceImpl.login()
        UserRepository.findByEmail(email)   [JdbcUserRepository: SELECT ... WHERE LOWER(email) = LOWER(?)]
        no such email:   UserNotFoundException, response 404
        wrong password:  BadCredentialsException, response 401
        success:         TokenService.issue(user) signs a token with the user's id
  Response 200: { token, user: { id, name, email, rollNumber } }
Login.jsx calls saveSession(), which stores token and user in localStorage
Browser goes to /dashboard
```

After login:
- `api.js` adds `Authorization: Bearer <token>` to every request.
- If any response is 401, `api.js` deletes the token and sends the user back to the login page.
- Logout in `Navbar.jsx` deletes the token and the user.
- Sign-up works the same way through `POST /api/auth/register` (inserts row with `INSERT INTO users ...` and retrieves auto-generated ID with `GeneratedKeyHolder`).

## 3. Dashboard

```
User opens /dashboard
Dashboard.jsx calls getDashboard() when it loads
  GET /api/events/dashboard   (token in header)
    Spring Security checks the token
    EventController.getDashboard()
      EventServiceImpl.getDashboardData()
        now = current time
        findLiveEvents(now)      [JdbcEventRepository: SELECT ... WHERE start_time <= ? AND end_time >= ?]
        findUpcomingEvents(now)  [JdbcEventRepository: SELECT ... WHERE start_time > ?]
        findPreviousEvents(now)  [JdbcEventRepository: SELECT ... WHERE end_time < ?]
  Response 200: { live: [...], upcoming: [...], previous: [...] }
Dashboard.jsx shows three sections of EventCard
```

The backend puts each event in a group, so the frontend never compares dates.
`EventCard.jsx` shows a Register Seat button only on upcoming events. The button is disabled when the event is full. Live and previous events show a disabled button.

## 4. Booking a seat

```
User clicks Register Seat on an upcoming event
Dashboard.jsx calls registerForEvent(eventId)
  POST /api/events/{eventId}/registrations   (token in header, no body)
    Spring Security checks the token
    RegistrationController reads the user id from the token
    RegistrationServiceImpl.registerUserForEvent(userId, eventId)   one @Transactional JDBC transaction
      1. UserRepository.findById(userId)           missing user: 404
      2. EventRepository.findByIdWithLock(eventId)
         SELECT ... FOR UPDATE locks the event row at the database level.
         A second booking for this event waits here.
      3. Already booked by this user?              [SELECT COUNT(*) FROM registrations WHERE user_id = ? AND event_id = ?] -> 409
      4. bookedSeats >= totalSeats?                400
      5. bookedSeats + 1, update event             [UPDATE events SET booked_seats = ... WHERE id = ?]
      6. Insert Registration row with timestamp    [INSERT INTO registrations ... (GeneratedKeyHolder)]
    Transaction commits, which releases the database lock
  Response 200: { registrationId, eventId, eventTitle, eventVenue,
                  eventStartTime, eventEndTime, registeredAt }
Dashboard.jsx shows a success message and reloads the seat counts
```

Without the lock, two requests could both read `bookedSeats = 49` for a 50-seat event. Both would pass the check and both would save 50. Two students would hold the last seat. The `SELECT ... FOR UPDATE` lock makes the second request wait until the first commits, so it reads 50 and gets "no seats".

## 5. Profile

```
User clicks Profile in the navbar
Profile.jsx calls getProfile() when it loads
  GET /api/me/profile   (token in header)
    Spring Security checks the token
    ProfileController reads the user id from the token
      ProfileServiceImpl.getProfileData(userId)
        UserRepository.findById(userId)
        RegistrationRepository.findByUserId(userId)
          [JdbcRegistrationRepository: SELECT r.*, e.* FROM registrations r JOIN events e ON r.event_id = e.id WHERE r.user_id = ? ORDER BY r.registered_at DESC]
  Response 200: { user: {...}, registrations: [...] }
Profile.jsx shows the user's details and a table of their bookings
```

## Layers

```
React app (port 5173 dev / bundled in static production)
  sends JSON with Axios, token in the Authorization header
Spring Security checks the token
Spring Boot API (port 8080 or cloud $PORT)
  AuthController          AuthService          UserRepository         (JdbcUserRepository)
  EventController         EventService         EventRepository        (JdbcEventRepository)
  RegistrationController  RegistrationService  RegistrationRepository (JdbcRegistrationRepository)
                                               EventRepository        (locked with FOR UPDATE)
  ProfileController       ProfileService       UserRepository, RegistrationRepository
Database: H2 (in-memory) or PostgreSQL, accessed via Spring JDBC (JdbcTemplate) & schema.sql
```

Each controller receives the request. Each service applies the rules. Each repository executes explicit, readable SQL queries via `JdbcTemplate` without ORM overhead.
