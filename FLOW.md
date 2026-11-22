# EventSphere request flows

This file follows each user action from the React page to the database and back.

## Overview
- The backend is a Spring Boot 3 REST API on port 8080.
- The frontend is a React app built with Vite on port 5173.
- The database is H2, held in memory. Its console is at `/h2-console`.

| Part | Starts in | What it does |
|---|---|---|
| Backend | `EventSphereApplication.java` | Starts Spring Boot and loads the test data |
| Frontend | `main.jsx`, then `App.jsx` | Renders the React app and sets up the page routes |

## 1. Startup

```
Backend starts: EventSphereApplication.main()
  SecurityConfig.java   login and sign-up are public, every other call needs a token
  JPA                   creates the H2 tables from the entity classes
  WebConfig.java        lets http://localhost:5173 call the API
  DataInitializer.run() adds 1 student account and 5 events

Browser opens http://localhost:5173
  main.jsx renders <App />
  App.jsx routes:
    /           Login.jsx
    /register   Register.jsx
    /dashboard  Dashboard.jsx  (needs a saved token)
    /profile    Profile.jsx    (needs a saved token)
```

## 2. Login

```
User enters email and password, clicks Login
Login.jsx calls loginUser(email, password) in api.js
  POST /api/auth/login   body: { email, password }
    AuthController.login()
      AuthServiceImpl.login()
        UserRepository.findByEmail(email)
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
- Sign-up works the same way through `POST /api/auth/register`.

## 3. Dashboard

```
User opens /dashboard
Dashboard.jsx calls getDashboard() when it loads
  GET /api/events/dashboard   (token in header)
    Spring Security checks the token
    EventController.getDashboard()
      EventServiceImpl.getDashboardData()
        now = current time
        findLiveEvents(now)      start <= now and end >= now
        findUpcomingEvents(now)  start > now
        findPreviousEvents(now)  end < now
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
    RegistrationServiceImpl.registerUserForEvent(userId, eventId)   one transaction
      1. UserRepository.findById(userId)           missing user: 404
      2. EventRepository.findByIdWithLock(eventId)
         SELECT ... FOR UPDATE locks the event row.
         A second booking for this event waits here.
      3. Already booked by this user?              409
      4. bookedSeats >= totalSeats?                400
      5. bookedSeats + 1, save the event
      6. Save a Registration row with the current time
    Transaction commits, which releases the lock
  Response 200: { registrationId, eventId, eventTitle, eventVenue,
                  eventStartTime, eventEndTime, registeredAt }
Dashboard.jsx shows a success message and reloads the seat counts
```

Without the lock, two requests could both read `bookedSeats = 49` for a 50-seat event. Both would pass the check and both would save 50. Two students would hold the last seat. The lock makes the second request wait until the first commits, so it reads 50 and gets "no seats".

## 5. Profile

```
User clicks Profile in the navbar
Profile.jsx calls getProfile() when it loads
  GET /api/me/profile   (token in header)
    Spring Security checks the token
    ProfileController reads the user id from the token
      ProfileServiceImpl.getProfileData(userId)
        UserRepository.findById(userId)
        RegistrationRepository.findByUserId(userId)   bookings joined with their events
  Response 200: { user: {...}, registrations: [...] }
Profile.jsx shows the user's details and a table of their bookings
```

## Layers

```
React app (port 5173)
  sends JSON with Axios, token in the Authorization header
Spring Security checks the token
Spring Boot API (port 8080)
  AuthController          AuthService          UserRepository
  EventController         EventService         EventRepository
  RegistrationController  RegistrationService  RegistrationRepository, EventRepository (locked)
  ProfileController       ProfileService       UserRepository, RegistrationRepository
H2 in-memory database, accessed through JPA and Hibernate
```

Each controller receives the request. Each service applies the rules. Each repository reads or writes the database.
