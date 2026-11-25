package com.eventsphere.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Automatically configures PostgreSQL DataSource if the {@code DATABASE_URL} environment variable
 * is supplied (such as by Render Managed PostgreSQL).
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource postgresDataSource(Environment env) {
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank() &&
                (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            log.info("Configuring PostgreSQL DataSource from DATABASE_URL");
            try {
                URI dbUri = new URI(databaseUrl.replace("postgres://", "postgresql://"));
                String username = "";
                String password = "";
                if (dbUri.getUserInfo() != null) {
                    String[] userInfo = dbUri.getUserInfo().split(":", 2);
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }
                int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
                String dbPath = dbUri.getPath();
                String host = dbUri.getHost();
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + dbPath;

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
                return new HikariDataSource(config);
            } catch (Exception e) {
                log.error("Failed to parse DATABASE_URL environment variable: {}", databaseUrl, e);
            }
        }

        // Fallback to standard Spring datasource configuration
        String url = env.getProperty("spring.datasource.url", "jdbc:h2:mem:eventspheredb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        String username = env.getProperty("spring.datasource.username", "sa");
        String password = env.getProperty("spring.datasource.password", "");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new HikariDataSource(config);
    }
}
