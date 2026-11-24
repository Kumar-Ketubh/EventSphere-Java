# EventSphere design decisions

Each decision below records the problem, what we chose, and what it costs us.

## ADR-001: separate React app and Spring Boot API

Status: accepted.

### Problem
We needed a prototype we could demo quickly. Two people had to work on the frontend and the backend at the same time.

### Decision
The backend is a Spring Boot 3 REST API on port 8080. It returns JSON and keeps no session.
The frontend is a React single-page app built with Vite on port 5173. It calls the API with Axios.

### Options we compared
| Concern | One server-rendered app (Thymeleaf) | React app plus REST API |
|---|---|---|
| Working in parallel | Frontend and backend share files | Each side works on its own |
| Reuse | The HTML only serves a browser | A mobile app could call the same API |
| UI | Server templates | React components |
| Setup | One process | Two processes |

### Cost
- The browser blocks calls between ports 5173 and 8080 unless the backend allows them. `WebConfig.java` allows them.
- You start two processes to run it locally: `mvn spring-boot:run` and `npm run dev`.

## ADR-002: lock the event row while booking a seat

Status: accepted.

### Problem
Booking a seat takes three steps:
1. Read `bookedSeats`.
2. Check that `bookedSeats` is less than `totalSeats`.
3. Add one to `bookedSeats` and save.

If two students book the last seat at the same moment, both requests can pass step 2. The event is then overbooked.

### Decision
`EventRepository.findByIdWithLock` locks the event row:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT e FROM Event e WHERE e.id = :id")
Optional<Event> findByIdWithLock(@Param("id") Long id);
```

The database runs this as `SELECT ... FOR UPDATE`. A second booking for the same event waits until the first one commits. It then reads the updated seat count.

### Options we compared
| Approach | Benefit | Drawback |
|---|---|---|
| Row lock (chosen) | Simple and always correct | Bookings for the same event wait in line |
| Version check (`@Version`) | Readers never wait | Needs code to retry when a save fails |
| Unique constraint on user and event | Stops one student booking twice | Does not stop overbooking |
| Redis lock | Works across several servers | Adds a server we don't need yet |

### Cost
- `registerUserForEvent` in `RegistrationServiceImpl` is `@Transactional`. The lock lasts until that method commits.
- A college event system gets few bookings per second, so the wait is short. A high-traffic system would switch to version checks with retries, or a booking queue.
- The seat count is stored twice: `bookedSeats` on the event, and one row per booking. The booking method updates both in the same transaction, so they stay equal.

## ADR-003: in-memory H2 database

Status: accepted.

### Problem
Reviewers and teammates need to run the project without installing a database server.

### Decision
The default configuration uses H2, a database that runs inside the Java process:

```properties
spring.datasource.url=jdbc:h2:mem:eventspheredb;DB_CLOSE_DELAY=-1
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
```

`DataInitializer.java` adds test data on every startup:
- One student account: `student@college.edu` with password `password123`.
- Five events. Their times are set relative to the current time, so the dashboard always has live, upcoming and previous events.

The project also includes the PostgreSQL driver. To use PostgreSQL, add `application-prod.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/eventsphere
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

Then run `java -jar eventsphere.jar --spring.profiles.active=prod`.

### Cost
- All data is deleted when the backend restarts.
- The H2 console at `http://localhost:8080/h2-console` lets you look at the tables during development.

## ADR-004: passwords stored as plain text

Status: accepted for the prototype only.

### Problem
Hashing passwords makes test accounts harder to seed and check by hand.

### Decision
`AuthServiceImpl` saves the password as typed and compares it with `equals` at login. The code marks each place with a `TODO: BCrypt` comment.

### Fix before real use
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Sign-up
user.setPassword(passwordEncoder.encode(request.getPassword()));

// Login
passwordEncoder.matches(request.getPassword(), user.getPassword());
```

### Cost
Anyone who can read the database can read every password. That is acceptable for an in-memory demo and never for real users.

## ADR-005: signed login tokens (JWT)

Status: accepted. This replaces the first version, which trusted a `userId` sent by the browser.

### Problem
The first version stored the user in the browser and sent `userId` with each request. The server believed that number. Anyone could change it and read another student's profile or book a seat in their name.

### Decision
- Login and sign-up return `{ token, user }`. `TokenService` signs the token with a secret key. The token holds the user's id and expires after 8 hours.
- `api.js` adds `Authorization: Bearer <token>` to every request.
- Spring Security checks the signature and the expiry on every request except login and sign-up. A missing, changed or expired token gets a 401 response.
- Controllers read the user's id from the token. No URL or request body carries a user id.

| Before | After |
|---|---|
| `GET /api/profile?userId=5` | `GET /api/me/profile` |
| `POST /api/events/7/register` with body `{ userId }` | `POST /api/events/7/registrations` with no body |

### Cost
- With no `EVENTSPHERE_JWT_SECRET` set, the app makes a random signing key on every startup. A restart logs everyone out. The in-memory database is wiped on restart too, so an old token would otherwise point at a user id that is gone or now belongs to someone else.
- In production, set `EVENTSPHERE_JWT_SECRET` to at least 32 random characters. The app refuses to start with a shorter one.
- The token is kept in `localStorage`. A script injected into the page could read it. An `HttpOnly` cookie would prevent that.
- There is no logout on the server. A token stays valid until it expires.

## Summary
| ADR | Decision | Reason |
|---|---|---|
| 001 | Separate React app and REST API | Frontend and backend built in parallel |
| 002 | Row lock when booking | Prevents overbooking |
| 003 | In-memory H2 with seed data | Runs with no database install |
| 004 | Plain-text passwords | Easy test data, prototype only |
| 005 | Signed login tokens | The server decides who the user is |
