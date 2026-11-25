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
| Setup | One process | Two processes (dev) / bundled jar (prod) |

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
`JdbcEventRepository.findByIdWithLock` locks the event row using native SQL:

```java
String sql = "SELECT id, title, description, venue, start_time, end_time, total_seats, booked_seats " +
             "FROM events WHERE id = ? FOR UPDATE";
List<Event> events = jdbcTemplate.query(sql, eventRowMapper, id);
```

The database executes this as `SELECT ... FOR UPDATE`. Within the `@Transactional` boundary of `registerUserForEvent`, a second booking for the same event waits until the first one commits. It then reads the updated seat count.

### Options we compared
| Approach | Benefit | Drawback |
|---|---|---|
| Row lock (`SELECT ... FOR UPDATE`, chosen) | Simple, explicit SQL, and always correct | Bookings for the same event wait in line |
| Version check (`@Version`) | Readers never wait | Needs code to retry when a save fails |
| Unique constraint on user and event | Stops one student booking twice | Does not stop overbooking |
| Redis lock | Works across several servers | Adds a server we don't need yet |

### Cost
- `registerUserForEvent` in `RegistrationServiceImpl` is `@Transactional`. The lock lasts until that method commits.
- A college event system gets few bookings per second, so the wait is short. A high-traffic system would switch to version checks with retries, or a booking queue.
- The seat count is stored twice: `bookedSeats` on the event, and one row per booking. The booking method updates both in the same transaction, so they stay equal.

## ADR-003: database setup and schema initialization

Status: accepted.

### Problem
Reviewers and teammates need to run the project without installing a database server, while production deployments on Render need persistent PostgreSQL support.

### Decision
The default configuration uses H2, a database that runs inside the Java process:

```properties
spring.datasource.url=jdbc:h2:mem:eventspheredb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.sql.init.mode=always
```

Tables are created on startup from `schema.sql` using portable ANSI SQL supported identically by H2 and PostgreSQL:
- `users`: stores accounts and credentials.
- `events`: stores schedules, venues, and capacity.
- `registrations`: stores confirmed bookings with foreign keys and unique constraints.

`DataInitializer.java` adds test data on startup if `userRepository.count() == 0`:
- One student account: `student@college.edu` with password `password123`.
- Five events (live, upcoming, and previous relative to current time).

In production (e.g., Render), connecting PostgreSQL via `DATABASE_URL` or `SPRING_DATASOURCE_*` switches the DataSource to PostgreSQL seamlessly without any schema alterations.

### Cost
- When using in-memory H2, all data is deleted when the backend restarts.
- The H2 console at `http://localhost:8080/h2-console` lets you inspect tables during development when enabled.

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
- With no `EVENTSPHERE_JWT_SECRET` set, the app makes a random signing key on every startup. A restart logs everyone out.
- In production, set `EVENTSPHERE_JWT_SECRET` to at least 32 random characters.
- The token is kept in `localStorage`.
- There is no logout on the server. A token stays valid until it expires.

## ADR-006: pure Spring JDBC instead of JPA/Hibernate

Status: accepted.

### Problem
Hibernate and Spring Data JPA introduced hidden complexity:
1. ORM proxying, lazy loading quirks (`LazyInitializationException`), and persistence context dirty checking.
2. Unnecessary overhead and slower startup times inside resource-constrained containers (Render free tier 512MB RAM).
3. Dialect conflicts and configuration friction when alternating between H2 (in-memory dev) and PostgreSQL (production).
4. Hidden queries and magic method derivation obscuring what SQL is actually executed.

### Decision
Replaced `spring-boot-starter-data-jpa` with `spring-boot-starter-jdbc` using Spring's `JdbcTemplate`:
- Entities (`User`, `Event`, `Registration`) are plain Java POJOs with no JPA/Jakarta annotations.
- Database schema is explicitly defined in ANSI SQL (`schema.sql`) and executed automatically via `spring.sql.init.mode=always`.
- Repositories (`JdbcUserRepository`, `JdbcEventRepository`, `JdbcRegistrationRepository`) execute explicit, clear SQL statements and map results using straightforward `RowMapper` definitions.
- Concurrency control in `findByIdWithLock` is an explicit, native `SELECT ... FOR UPDATE` query executed within a `@Transactional` boundary (`DataSourceTransactionManager`).
- Complex reads (such as ticket verification and profile registration history) use explicit SQL `JOIN`s, eliminating N+1 query surprises.

### Options we compared
| Concern | JPA / Hibernate | Pure Spring JDBC (Chosen) |
|---|---|---|
| Transparency | Hidden queries & proxy generation | 100% explicit SQL statements |
| Startup & Memory | Heavier footprint, bytecode parsing | Fast startup, minimal memory overhead |
| Cross-DB Compatibility | Dialect-dependent mappings | Standard ANSI SQL schema and queries |
| Debugging | Stack traces through ORM layers | Direct SQL errors pinpointed immediately |
| Maintenance | High cognitive load | Simple, accessible for students/evaluators |

### Cost
- Repositories write SQL queries and RowMappers explicitly rather than relying on automatic method name query derivation (`findBy...`).

## Summary
| ADR | Decision | Reason |
|---|---|---|
| 001 | Separate React app and REST API | Frontend and backend built in parallel |
| 002 | Row lock when booking (`FOR UPDATE`) | Prevents overbooking |
| 003 | H2/PostgreSQL with `schema.sql` | Runs with zero install or on cloud PostgreSQL |
| 004 | Plain-text passwords | Easy test data, prototype only |
| 005 | Signed login tokens (JWT) | The server decides who the user is |
| 006 | Pure Spring JDBC (`JdbcTemplate`) | Simplicity, explicit SQL, and lightweight runtime |
