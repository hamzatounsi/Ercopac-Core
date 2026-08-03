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
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    private static final String[] PLATFORM_ROLES = {
            "PLATFORM_OWNER", "ROLE_PLATFORM_OWNER",
            "PLATFORM_ADMIN", "ROLE_PLATFORM_ADMIN"
    };

    private static final String[] ORG_ADMIN_ROLES = {
            "PLATFORM_OWNER", "ROLE_PLATFORM_OWNER",
            "PLATFORM_ADMIN", "ROLE_PLATFORM_ADMIN",
            "ORG_ADMIN", "ROLE_ORG_ADMIN"
    };

    private static final String[] ORG_ADMIN_ONLY = {"ORG_ADMIN", "ROLE_ORG_ADMIN"};

    private static final String[] MANAGER_ROLES = {
            "PLATFORM_OWNER", "ROLE_PLATFORM_OWNER",
            "GENERAL_MANAGER", "ROLE_GENERAL_MANAGER",
            "DEPARTMENT_MANAGER", "ROLE_DEPARTMENT_MANAGER"
    };

    private static final String[] EMPLOYEE_ROLES = {"EMPLOYEE", "ROLE_EMPLOYEE"};

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Authentication is required."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "You do not have permission to access this resource."))
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/api/auth/password-reset/pending",
                                "/api/auth/password-reset/*/approve",
                                "/api/auth/password-reset/*/reject"
                        )
                        .hasAnyAuthority(ORG_ADMIN_ONLY)

                        .requestMatchers(
                                "/api/auth/**",
                                "/ws/tickets/**",
                                "/api/health",
                                "/error"
                        ).permitAll()

                        .requestMatchers("/api/platform/**")
                        .hasAnyAuthority(PLATFORM_ROLES)

                        .requestMatchers("/api/org-admin/**")
                        .hasAnyAuthority(ORG_ADMIN_ONLY)

                        .requestMatchers("/api/admin/**")
                        .hasAnyAuthority(ORG_ADMIN_ROLES)

                        .requestMatchers(
                                "/api/gm/**",
                                "/api/crm/**",
                                "/api/projects/**",
                                "/api/tasks/**",
                                "/api/resources/**",
                                "/api/suppliers/**",
                                "/api/department/**",
                                "/api/department-dashboard/**",
                                "/api/resource-config/**",
                                "/api/finance/**",
                                "/api/risks/**",
                                "/api/ai/**"
                        )
                        .hasAnyAuthority(MANAGER_ROLES)

                        .requestMatchers("/api/employee/**")
                        .hasAnyAuthority(EMPLOYEE_ROLES)

                        .anyRequest().authenticated()
                );

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

    private static void writeSecurityError(
            HttpServletResponse response,
            int status,
            String error,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status
                        + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}"
        );
    }
}
