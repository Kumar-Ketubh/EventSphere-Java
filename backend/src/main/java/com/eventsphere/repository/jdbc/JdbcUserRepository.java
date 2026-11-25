package com.eventsphere.repository.jdbc;

import com.eventsphere.entity.User;
import com.eventsphere.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

/**
 * Pure JDBC implementation of {@link UserRepository} using {@link JdbcTemplate}.
 */
@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .email(rs.getString("email"))
            .password(rs.getString("password"))
            .rollNumber(rs.getString("roll_number"))
            .build();

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, name, email, password, roll_number FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, password, roll_number FROM users WHERE LOWER(email) = LOWER(?)";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            String sql = "INSERT INTO users (name, email, password, roll_number) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getRollNumber());
                return ps;
            }, keyHolder);

            Long generatedId = extractGeneratedKey(keyHolder);
            if (generatedId != null) {
                user.setId(generatedId);
            }
            return user;
        } else {
            String sql = "UPDATE users SET name = ?, email = ?, password = ?, roll_number = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRollNumber(),
                    user.getId());
            return user;
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
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
