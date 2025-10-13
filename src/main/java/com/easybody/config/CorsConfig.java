package com.easybody.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for EasyBody API
 * 
 * Allows frontend applications to call backend APIs from different origins.
 * 
 * Security Notes:
 * - In production, specify exact frontend URLs instead of "*"
 * - Use environment variables to configure allowed origins
 * - Credentials (cookies, auth headers) are allowed for authenticated requests
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:3001,https://aws-ws-fe.vercel.app}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ Allowed Origins - Frontend URLs that can call this API
        // Development: localhost:3000, localhost:3001
        // Production: https://aws-ws-fe.vercel.app
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        // ✅ Allowed HTTP Methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));
        
        // ✅ Allowed Headers - Frontend can send these headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));
        
        // ✅ Exposed Headers - Frontend can read these headers from response
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));
        
        // ✅ Allow Credentials - Required for JWT tokens in Authorization header
        configuration.setAllowCredentials(true);
        
        // ✅ Max Age - Browser can cache preflight request for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
