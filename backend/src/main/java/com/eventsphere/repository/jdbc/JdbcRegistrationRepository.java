package com.eventsphere.repository.jdbc;

import com.eventsphere.entity.Event;
import com.eventsphere.entity.Registration;
import com.eventsphere.entity.User;
import com.eventsphere.repository.RegistrationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pure JDBC implementation of {@link RegistrationRepository} using {@link JdbcTemplate}.
 */
@Repository
public class JdbcRegistrationRepository implements RegistrationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRegistrationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByUserIdAndEventId(Long userId, Long eventId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND event_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, eventId);
        return count != null && count > 0;
    }

    @Override
    public List<Registration> findByUserId(Long userId) {
        String sql = "SELECT r.id AS reg_id, r.user_id, r.event_id, r.registered_at, " +
                "e.id AS e_id, e.title, e.description, e.venue, e.start_time, e.end_time, e.total_seats, e.booked_seats " +
                "FROM registrations r " +
                "JOIN events e ON r.event_id = e.id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.registered_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp regTime = rs.getTimestamp("registered_at");
            Timestamp startTime = rs.getTimestamp("start_time");
            Timestamp endTime = rs.getTimestamp("end_time");

            Event event = Event.builder()
                    .id(rs.getLong("e_id"))
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .venue(rs.getString("venue"))
                    .startTime(startTime != null ? startTime.toLocalDateTime() : null)
                    .endTime(endTime != null ? endTime.toLocalDateTime() : null)
                    .totalSeats(rs.getInt("total_seats"))
                    .bookedSeats(rs.getInt("booked_seats"))
                    .build();

            User user = User.builder().id(rs.getLong("user_id")).build();

            return Registration.builder()
                    .id(rs.getLong("reg_id"))
                    .user(user)
                    .event(event)
                    .registeredAt(regTime != null ? regTime.toLocalDateTime() : null)
                    .build();
        }, userId);
    }

    @Override
    public Optional<Registration> findWithDetailsById(Long id) {
        String sql = "SELECT r.id AS reg_id, r.registered_at, " +
                "u.id AS u_id, u.name AS u_name, u.email AS u_email, u.roll_number AS u_roll, " +
                "e.id AS e_id, e.title, e.description, e.venue, e.start_time, e.end_time, e.total_seats, e.booked_seats " +
                "FROM registrations r " +
                "JOIN users u ON r.user_id = u.id " +
                "JOIN events e ON r.event_id = e.id " +
                "WHERE r.id = ?";

        List<Registration> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp regTime = rs.getTimestamp("registered_at");
            Timestamp startTime = rs.getTimestamp("start_time");
            Timestamp endTime = rs.getTimestamp("end_time");

            User user = User.builder()
                    .id(rs.getLong("u_id"))
                    .name(rs.getString("u_name"))
                    .email(rs.getString("u_email"))
                    .rollNumber(rs.getString("u_roll"))
                    .build();

            Event event = Event.builder()
                    .id(rs.getLong("e_id"))
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .venue(rs.getString("venue"))
                    .startTime(startTime != null ? startTime.toLocalDateTime() : null)
                    .endTime(endTime != null ? endTime.toLocalDateTime() : null)
                    .totalSeats(rs.getInt("total_seats"))
                    .bookedSeats(rs.getInt("booked_seats"))
                    .build();

            return Registration.builder()
                    .id(rs.getLong("reg_id"))
                    .user(user)
                    .event(event)
                    .registeredAt(regTime != null ? regTime.toLocalDateTime() : null)
                    .build();
        }, id);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Registration> findById(Long id) {
        return findWithDetailsById(id);
    }

    @Override
    public Registration save(Registration registration) {
        LocalDateTime regTime = registration.getRegisteredAt() != null
                ? registration.getRegisteredAt()
                : LocalDateTime.now();
        registration.setRegisteredAt(regTime);

        if (registration.getId() == null) {
            String sql = "INSERT INTO registrations (user_id, event_id, registered_at) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, registration.getUser().getId());
                ps.setLong(2, registration.getEvent().getId());
                ps.setTimestamp(3, Timestamp.valueOf(regTime));
                return ps;
            }, keyHolder);

            Long generatedId = extractGeneratedKey(keyHolder);
            if (generatedId != null) {
                registration.setId(generatedId);
            }
            return registration;
        } else {
            String sql = "UPDATE registrations SET user_id = ?, event_id = ?, registered_at = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    registration.getUser().getId(),
                    registration.getEvent().getId(),
                    Timestamp.valueOf(regTime),
                    registration.getId());
            return registration;
        }
    }

    @Override
    public void delete(Registration registration) {
        if (registration != null && registration.getId() != null) {
            String sql = "DELETE FROM registrations WHERE id = ?";
            jdbcTemplate.update(sql, registration.getId());
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM registrations";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    private Long extractGeneratedKey(KeyHolder keyHolder) {
        try {
            Number key = keyHolder.getKey();
            if (key != null) {
                return key.longValue();
            }
        } catch (Exception ignored) {
        }
        if (keyHolder.getKeys() != null) {
            for (Object val : keyHolder.getKeys().values()) {
                if (val instanceof Number) {
                    return ((Number) val).longValue();
                }
            }
        }
        return null;
    }
}
