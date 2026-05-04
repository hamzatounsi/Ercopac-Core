package com.ercopac.ercopac_tracker.config;

import com.ercopac.ercopac_tracker.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                .requestMatchers("/api/auth/**", "/api/health").permitAll()

                .requestMatchers("/api/platform/**")
                .hasAnyAuthority("PLATFORM_OWNER", "ROLE_PLATFORM_OWNER")

                .requestMatchers("/api/org-admin/**")
                .hasAnyRole("ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers("/api/admin/**")
                .hasAnyRole("ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers("/api/gm/**")
                .hasAnyRole("GENERAL_MANAGER", "ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers("/api/resources", "/api/resources/**")
                .hasAnyRole("GENERAL_MANAGER", "ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers("/api/suppliers/**")
                    .hasAnyRole("GENERAL_MANAGER", "ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers("/api/projects/**")
                .hasAnyRole("GENERAL_MANAGER", "ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers(
                    "/api/projects/*/baselines",
                    "/api/projects/*/baselines/**",
                    "/api/projects/*/calendars",
                    "/api/projects/*/calendars/**",
                    "/api/projects/*/templates",
                    "/api/projects/*/templates/**"
                )
                    .hasAnyRole("GENERAL_MANAGER", "ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers("/api/admin/**")
                .hasAnyAuthority(
                    "ROLE_PLATFORM_OWNER",
                    "ROLE_ORG_ADMIN",
                    "ORG_ADMIN"
                )

                .requestMatchers("/api/department/**")
                    .hasAnyRole("DEPARTMENT_MANAGER", "GENERAL_MANAGER", "ORG_ADMIN", "PLATFORM_OWNER")

                .requestMatchers("/api/employee/**")
                    .hasRole("EMPLOYEE")

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