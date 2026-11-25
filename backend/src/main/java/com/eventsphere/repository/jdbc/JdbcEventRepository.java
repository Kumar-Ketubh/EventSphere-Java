package com.eventsphere.repository.jdbc;

import com.eventsphere.entity.Event;
import com.eventsphere.repository.EventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pure JDBC implementation of {@link EventRepository} using {@link JdbcTemplate}.
 */
@Repository
public class JdbcEventRepository implements EventRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Event> eventRowMapper = (rs, rowNum) -> {
        Timestamp startTimestamp = rs.getTimestamp("start_time");
        Timestamp endTimestamp = rs.getTimestamp("end_time");
        return Event.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .venue(rs.getString("venue"))
                .startTime(startTimestamp != null ? startTimestamp.toLocalDateTime() : null)
                .endTime(endTimestamp != null ? endTimestamp.toLocalDateTime() : null)
                .totalSeats(rs.getInt("total_seats"))
                .bookedSeats(rs.getInt("booked_seats"))
                .build();
    };

    public JdbcEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Event> findById(Long id) {
        String sql = "SELECT id, title, description, venue, start_time, end_time, total_seats, booked_seats FROM events WHERE id = ?";
        List<Event> events = jdbcTemplate.query(sql, eventRowMapper, id);
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(0));
    }

    @Override
    public Optional<Event> findByIdWithLock(Long id) {
        // Native SELECT ... FOR UPDATE provides row-level pessimistic write lock
        String sql = "SELECT id, title, description, venue, start_time, end_time, total_seats, booked_seats FROM events WHERE id = ? FOR UPDATE";
        List<Event> events = jdbcTemplate.query(sql, eventRowMapper, id);
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(0));
    }

    @Override
    public List<Event> findLiveEvents(LocalDateTime now) {
        String sql = "SELECT id, title, description, venue, start_time, end_time, total_seats, booked_seats FROM events WHERE start_time <= ? AND end_time >= ? ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, eventRowMapper, Timestamp.valueOf(now), Timestamp.valueOf(now));
    }

    @Override
    public List<Event> findUpcomingEvents(LocalDateTime now) {
        String sql = "SELECT id, title, description, venue, start_time, end_time, total_seats, booked_seats FROM events WHERE start_time > ? ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, eventRowMapper, Timestamp.valueOf(now));
    }

    @Override
    public List<Event> findPreviousEvents(LocalDateTime now) {
        String sql = "SELECT id, title, description, venue, start_time, end_time, total_seats, booked_seats FROM events WHERE end_time < ? ORDER BY end_time DESC";
        return jdbcTemplate.query(sql, eventRowMapper, Timestamp.valueOf(now));
    }

    @Override
    public List<Event> findAll() {
        String sql = "SELECT id, title, description, venue, start_time, end_time, total_seats, booked_seats FROM events ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, eventRowMapper);
    }

    @Override
    public Event save(Event event) {
        if (event.getId() == null) {
            String sql = "INSERT INTO events (title, description, venue, start_time, end_time, total_seats, booked_seats) VALUES (?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, event.getTitle());
                ps.setString(2, event.getDescription());
                ps.setString(3, event.getVenue());
                ps.setTimestamp(4, Timestamp.valueOf(event.getStartTime()));
                ps.setTimestamp(5, Timestamp.valueOf(event.getEndTime()));
                ps.setInt(6, event.getTotalSeats());
                ps.setInt(7, event.getBookedSeats());
                return ps;
            }, keyHolder);

            Long generatedId = extractGeneratedKey(keyHolder);
            if (generatedId != null) {
                event.setId(generatedId);
            }
            return event;
        } else {
            String sql = "UPDATE events SET title = ?, description = ?, venue = ?, start_time = ?, end_time = ?, total_seats = ?, booked_seats = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    event.getTitle(),
                    event.getDescription(),
                    event.getVenue(),
                    Timestamp.valueOf(event.getStartTime()),
                    Timestamp.valueOf(event.getEndTime()),
                    event.getTotalSeats(),
                    event.getBookedSeats(),
                    event.getId());
            return event;
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM events";
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
