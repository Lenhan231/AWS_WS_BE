package com.easybody.controller;

import com.easybody.dto.request.UserRegistrationRequest;
import com.easybody.dto.response.UserResponse;
import com.easybody.model.enums.Role;
import com.easybody.service.UserService;
import com.easybody.config.LocalBasicAuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final LocalBasicAuthProperties localBasicAuthProperties;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegistrationRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Attempted registration without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String cognitoSub = authentication.getName();
        UserResponse response = userService.createUser(cognitoSub, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get profile of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Attempted profile access without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // In local development with HTTP Basic, map Basic username to seeded admin
        // Prefer explicit Basic header detection for local development
        if ((authorization != null && authorization.startsWith("Basic "))
                || "local-admin".equals(authentication.getName())) {
            try {
                UserResponse response = userService.getUserByCognitoSub("seed-admin-sub");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                log.warn("Local basic auth: seeded admin not found in DB, returning stub user");
                UserResponse stub = UserResponse.builder()
                        .id(null)
                        .email("admin@easybody.com")
                        .firstName("System")
                        .lastName("Admin")
                        .phoneNumber(null)
                        .role(Role.ADMIN)
                        .active(true)
                        .profileImageUrl(null)
                        .createdAt(null)
                        .updatedAt(null)
                        .build();
                return ResponseEntity.ok(stub);
            }
        }

        String cognitoSub = authentication.getName();
        UserResponse response = userService.getUserByCognitoSub(cognitoSub);
        return ResponseEntity.ok(response);
    }
}
