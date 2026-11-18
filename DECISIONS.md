# EventSphere MVP v1 — Architectural Decision Record (ADR)

---

## ADR-001: Decoupled React SPA + Spring Boot REST API Architecture

**Status:** Accepted

**Context:**
The project needed a prototype that could be demoed quickly, developed independently on both ends, and extended later without major restructuring.

**Decision:**
Adopt a fully decoupled architecture:
- **Backend:** Spring Boot 3.x exposing a stateless JSON REST API on port `8080`.
- **Frontend:** React SPA (Vite) consuming the API via Axios on port `5173`.

**Rationale:**
| Concern               | Monolith (Thymeleaf)         | Decoupled SPA + REST API          |
|-----------------------|------------------------------|-----------------------------------|
| Team independence     | Tightly coupled               | Backend & frontend develop in parallel |
| API reusability       | None (server-rendered)        | Same API can serve mobile clients |
| UI flexibility        | Limited (server template)     | Full React component ecosystem    |
| Testing               | Integration-heavy             | Unit + contract tests independent |
| Demo speed            | Fast (single deploy)          | Marginally more setup, far more flexible |

**Consequences:**
- CORS must be explicitly configured (handled in `WebConfig.java`).
- Auth state is managed client-side in `localStorage` for MVP simplicity (can be upgraded to JWT/session cookies in v2).
- Two separate processes to start for local development (`mvn spring-boot:run` + `npm run dev`).

---

## ADR-002: Pessimistic Locking for Seat Booking to Prevent Overbooking

**Status:** Accepted

**Context:**
The seat registration feature involves a classic "check-then-act" race condition:
1. Read current `bookedSeats`.
2. Verify `bookedSeats < totalSeats`.
3. Increment `bookedSeats` and save.

Under concurrent requests, steps 1–3 are not atomic by default, allowing multiple users to book the last seat simultaneously.

**Decision:**
Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` on the `findByIdWithLock` query in `EventRepository`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT e FROM Event e WHERE e.id = :id")
Optional<Event> findByIdWithLock(@Param("id") Long id);
```

This translates to `SELECT ... FOR UPDATE` at the database level, serializing concurrent booking requests for the same event row.

**Alternatives Considered:**

| Approach                        | Pros                              | Cons                                        |
|---------------------------------|-----------------------------------|---------------------------------------------|
| **Pessimistic Lock (chosen)**   | Simple, guaranteed correctness    | Blocks concurrent requests (acceptable for MVP) |
| Optimistic Lock (`@Version`)    | Non-blocking reads                | Requires retry logic on `OptimisticLockException` |
| DB `UNIQUE` constraint only     | Handles duplicates per user       | Does not prevent total overbooking          |
| Redis distributed lock          | Scalable across instances         | External dependency, overkill for MVP       |

**Consequences:**
- For low to moderate concurrency (expected for an academic event system), pessimistic locking is safe and simple.
- The `@Transactional` boundary on `RegistrationServiceImpl.registerUserForEvent()` ensures the lock is held only for the duration of the booking transaction and released on commit.
- For high-traffic production use, upgrade to optimistic locking + retry or a queue-based booking system.

---

## ADR-003: H2 In-Memory Database for Zero-Setup Local Execution

**Status:** Accepted

**Context:**
Reviewers, TAs, and team members need to run the prototype without installing or configuring an external database server.

**Decision:**
Use Spring Boot's embedded **H2 in-memory database** as the default profile:

```properties
spring.datasource.url=jdbc:h2:mem:eventspheredb;DB_CLOSE_DELAY=-1
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
```

**Data Initialization:**
`DataInitializer.java` (implementing `CommandLineRunner`) seeds all required test data on startup, including:
- 1 sample student user (`student@college.edu` / `password123`)
- 5 test events spread across Live, Upcoming, and Previous timelines relative to `LocalDateTime.now()`

**PostgreSQL Readiness:**
The project includes PostgreSQL driver and dependency. Switching is a one-property change:
```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/eventsphere
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

Run with: `java -jar eventsphere.jar --spring.profiles.active=prod`

**Consequences:**
- **Zero external dependencies** for running the MVP locally.
- Data is lost on every restart (by design for a demo/prototype).
- H2 console is accessible at `http://localhost:8080/h2-console` for manual inspection during development.

---

## ADR-004: Plain Password Storage for MVP (Upgrade Path Noted)

**Status:** Accepted (MVP only)

**Context:**
Spring Security requires a `PasswordEncoder` bean. For the MVP demo, configuring BCrypt hashing adds friction for manual testing (can't easily inspect or seed passwords).

**Decision:**
Use `{noop}` prefix with `NoOpPasswordEncoder` for MVP:
```java
// Registration — store as-is for MVP
user.setPassword(request.getPassword());
```

**Upgrade Path (v2):**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
// Registration
user.setPassword(passwordEncoder.encode(request.getPassword()));
```

**Consequences:**
- Passwords stored in plaintext in H2 (acceptable for in-memory demo, never for production).
- Clearly flagged with `// TODO: Replace with BCryptPasswordEncoder in production` comments in code.

---

## ADR-005: Client-Side Auth State via localStorage (MVP Simplification)

**Status:** Accepted (MVP only)

**Context:**
Full JWT or server-side session management requires additional infrastructure (token refresh, CSRF protection, HttpOnly cookies). For the MVP, the goal is rapid end-to-end demonstration.

**Decision:**
On successful login, store the user object in `localStorage`:
```js
localStorage.setItem("user", JSON.stringify(response.data));
```
Protected routes in React check for this key; if absent, redirect to `/`.

**Upgrade Path (v2):**
- Issue JWT tokens from backend on login.
- Store token in `HttpOnly` cookie (XSS-safe).
- Add `Authorization: Bearer <token>` header via Axios interceptor.
- Spring Security validates token on every request.

---

## Summary Table

| ADR | Decision                                | Driver                        |
|-----|-----------------------------------------|-------------------------------|
| 001 | Decoupled SPA + REST API                | Separation of concerns, reusability |
| 002 | Pessimistic locking for seat booking    | Data integrity, overbooking prevention |
| 003 | H2 in-memory DB + DataInitializer seed  | Zero-setup prototype execution |
| 004 | NoOp password encoder                  | MVP demo simplicity            |
| 005 | localStorage for auth state             | MVP demo simplicity            |
