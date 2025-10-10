package com.easybody.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ✅ Swagger/OpenAPI (không cần token)
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/actuator/health"
                ).permitAll()

                // ✅ Public endpoints đang có
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/search/**",
                    "/api/v1/gyms/search",
                    "/api/v1/gyms/{id}",
                    "/api/v1/pt-users/{id}",
                    "/api/v1/offers/{id}",
                    "/api/v1/ratings/offer/{offerId}"
                ).permitAll()

                // ✅ Role-based business rules
                .requestMatchers(HttpMethod.POST, "/api/v1/gyms").hasAuthority("GYM_STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/v1/gyms/**").hasAuthority("GYM_STAFF")
                .requestMatchers(HttpMethod.POST, "/api/v1/gyms/**/assign-pt").hasAuthority("GYM_STAFF")

                // ✅ Admin-only
                .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")

                // ❗ Các endpoint còn lại yêu cầu token
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
