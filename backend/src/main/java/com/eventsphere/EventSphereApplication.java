package com.eventsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EventSphere MVP v1 — Application Entry Point.
 *
 * <p>Bootstraps the Spring Boot context, auto-creates the H2 schema via JPA,
 * and triggers {@link com.eventsphere.config.DataInitializer} to seed sample data.</p>
 *
 * <p>Run with: {@code mvn spring-boot:run} from the {@code backend/} directory.</p>
 * <p>H2 Console: <a href="http://localhost:8080/h2-console">http://localhost:8080/h2-console</a></p>
 */
@SpringBootApplication
public class EventSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventSphereApplication.class, args);
    }
}
