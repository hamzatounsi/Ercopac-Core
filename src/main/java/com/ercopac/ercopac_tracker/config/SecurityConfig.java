package com.ercopac.ercopac_tracker.config;

import com.ercopac.ercopac_tracker.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> {});

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers(
                        "/api/auth/**",
                        "/api/health"
                ).permitAll()

                // TEMPORARY TEST ACCESS
                .requestMatchers(HttpMethod.PUT, "/api/tasks/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/tasks/**").permitAll()
                .requestMatchers("/api/tasks/**").permitAll()

                .requestMatchers("/api/gm/projects/*/schedule/history/**")
                .hasAnyAuthority(
                        "GENERAL_MANAGER",
                        "ROLE_GENERAL_MANAGER",
                        "ORG_ADMIN",
                        "ROLE_ORG_ADMIN",
                        "PLATFORM_OWNER",
                        "ROLE_PLATFORM_OWNER",
                        "DEPARTMENT_MANAGER",
                        "ROLE_DEPARTMENT_MANAGER"
                )

                .requestMatchers("/api/platform/**")
                .hasAnyAuthority(
                        "PLATFORM_OWNER",
                        "ROLE_PLATFORM_OWNER"
                )

                .requestMatchers(
                        "/api/gm/**",
                        "/api/projects/**",
                        "/api/resources",
                        "/api/resources/**",
                        "/api/suppliers/**"
                )
                .hasAnyAuthority(
                        "GENERAL_MANAGER",
                        "ROLE_GENERAL_MANAGER",
                        "ORG_ADMIN",
                        "ROLE_ORG_ADMIN",
                        "PLATFORM_OWNER",
                        "ROLE_PLATFORM_OWNER",
                        "DEPARTMENT_MANAGER",
                        "ROLE_DEPARTMENT_MANAGER"
                )

                .requestMatchers("/api/org-admin/**", "/api/admin/**")
                .hasAnyAuthority(
                        "ORG_ADMIN",
                        "ROLE_ORG_ADMIN",
                        "PLATFORM_OWNER",
                        "ROLE_PLATFORM_OWNER"
                )

                .requestMatchers("/api/department/**")
                .hasAnyAuthority(
                        "DEPARTMENT_MANAGER",
                        "ROLE_DEPARTMENT_MANAGER",
                        "GENERAL_MANAGER",
                        "ROLE_GENERAL_MANAGER",
                        "ORG_ADMIN",
                        "ROLE_ORG_ADMIN",
                        "PLATFORM_OWNER",
                        "ROLE_PLATFORM_OWNER"
                )

                .requestMatchers("/api/employee/**")
                .hasAnyAuthority(
                        "EMPLOYEE",
                        "ROLE_EMPLOYEE"
                )

                .anyRequest().authenticated()
        );

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}