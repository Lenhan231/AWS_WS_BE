package com.easybody.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(LocalBasicAuthProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LocalBasicAuthProperties localBasicAuthProperties;

    @Bean
    public UserDetailsService userDetailsService() {
        if (localBasicAuthProperties.isEnabled()) {
            UserDetails user1 = User.withUsername(localBasicAuthProperties.getUsername())
                .password("{noop}" + localBasicAuthProperties.getPassword())
                .authorities(new SimpleGrantedAuthority(localBasicAuthProperties.getRole()))
                .build();
            // Add a second local user whose username equals the seeded admin cognito_sub,
            // so that Authentication#getName() matches DB lookup in /auth/me.
            UserDetails user2 = User.withUsername("seed-admin-sub")
                .password("{noop}" + localBasicAuthProperties.getPassword())
                .authorities(new SimpleGrantedAuthority(localBasicAuthProperties.getRole()))
                .build();
            return new InMemoryUserDetailsManager(user1, user2);
        }

        // Empty manager prevents Spring Boot from generating a default user/password when Basic auth is disabled
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (localBasicAuthProperties.isEnabled()) {
            http.httpBasic(Customizer.withDefaults());
        }

        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // ✅ Enable CORS with CorsConfig
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
                    "/api/v1/search/**",
                    "/api/v1/gyms/search",
                    "/api/v1/gyms/{id}",
                    "/api/v1/pt-users/{id}",
                    "/api/v1/offers/{id}",
                    "/api/v1/ratings/offer/{offerId}"
                ).permitAll()

                // ✅ Auth endpoints require a valid JWT (Cognito)
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/me").authenticated()

                // ✅ Role-based business rules
                .requestMatchers(HttpMethod.POST, "/api/v1/gyms").hasAuthority("GYM_STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/v1/gyms/**").hasAuthority("GYM_STAFF")
                // Use Spring MVC path pattern (no ** followed by more data)
                .requestMatchers(HttpMethod.POST, "/api/v1/gyms/{id}/assign-pt").hasAuthority("GYM_STAFF")

                // ✅ Admin-only
                .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")

                // ❗ Các endpoint còn lại yêu cầu token
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
