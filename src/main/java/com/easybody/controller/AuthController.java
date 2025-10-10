package com.easybody.controller;

import com.easybody.dto.request.UserRegistrationRequest;
import com.easybody.dto.response.UserResponse;
import com.easybody.service.UserService;
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

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegistrationRequest request,
            Authentication authentication) {

        String cognitoSub = authentication.getName();
        UserResponse response = userService.createUser(cognitoSub, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get profile of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String cognitoSub = authentication.getName();
        UserResponse response = userService.getUserByCognitoSub(cognitoSub);
        return ResponseEntity.ok(response);
    }
}
