package com.eventsphere.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC configuration for CORS (Cross-Origin Resource Sharing).
 *
 * <p>Allows the React Vite dev server running on {@code http://localhost:5173}
 * to make cross-origin requests to the Spring Boot API on {@code http://localhost:8080}.</p>
 *
 * <p>Permitted methods cover all REST operations used by the frontend:
 * GET (dashboard, profile), POST (auth, registration).</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers global CORS mappings for all API endpoints.
     *
     * <p>For production, replace {@code http://localhost:5173} with the deployed
     * frontend URL and restrict {@code allowedOrigins} accordingly.</p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
