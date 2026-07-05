package com.demo.offline_upi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Config setting up endpoint authorization rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // Allows H2 console
            .authorizeHttpRequests(auth -> auth
                // Allow public access to dashboard, H2 console, and server key info
                .requestMatchers("/", "/index.html", "/h2-console/**", "/api/server-key", "/api/accounts").permitAll()
                // Inbound bridge endpoints require ROLE_BRIDGE
                .requestMatchers("/api/bridge/**").hasRole("BRIDGE")
                // Admin control / Simulator endpoints require ROLE_ADMIN
                .requestMatchers("/api/mesh/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new BridgeAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
