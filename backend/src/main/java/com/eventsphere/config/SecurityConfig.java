package com.eventsphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for EventSphere MVP.
 *
 * <p>For rapid prototype testing, all endpoints are permit-all (no auth filter).
 * CSRF is disabled to allow Axios POST requests without CSRF tokens.
 * The H2 console frame options are permitted for the browser console UI.</p>
 *
 * <p>TODO (v2): Replace with JWT filter chain and role-based access control.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain:
     * <ul>
     *   <li>All requests permitted (MVP — no authentication enforcement)</li>
     *   <li>CSRF disabled (required for stateless REST API + Axios)</li>
     *   <li>Frame options set to SAMEORIGIN (required for H2 console iframe)</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }
}
