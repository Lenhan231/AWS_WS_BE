package com.easybody.controller;

import com.easybody.dto.request.PTUserCreateRequest;
import com.easybody.dto.request.PTUserUpdateRequest;
import com.easybody.dto.response.GymPTAssociationResponse;
import com.easybody.dto.response.PTUserResponse;
import com.easybody.service.GymPTAssociationService;
import com.easybody.service.PTUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pt-users")
@RequiredArgsConstructor
@Slf4j
public class PTUserController {

    private final PTUserService ptUserService;
    private final GymPTAssociationService associationService;

    @PostMapping
    @PreAuthorize("hasAuthority('PT_USER')")
    public ResponseEntity<PTUserResponse> createPTProfile(
            @Valid @RequestBody PTUserCreateRequest request,
            Authentication authentication) {

        PTUserResponse response = ptUserService.createPTProfile(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{ptUserId}")
    @PreAuthorize("hasAuthority('PT_USER') or hasAuthority('ADMIN')")
    public ResponseEntity<PTUserResponse> updatePTProfile(
            @PathVariable Long ptUserId,
            @Valid @RequestBody PTUserUpdateRequest request) {

        PTUserResponse response = ptUserService.updatePTProfile(ptUserId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ptUserId}")
    public ResponseEntity<PTUserResponse> getPTUserById(@PathVariable Long ptUserId) {
        PTUserResponse response = ptUserService.getPTUserById(ptUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PTUserResponse>> getAllPTUsers(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10") Double radiusKm) {

        if (latitude != null && longitude != null) {
            List<PTUserResponse> response = ptUserService.findPTUsersNearLocation(latitude, longitude, radiusKm);
            return ResponseEntity.ok(response);
        } else {
            List<PTUserResponse> response = ptUserService.getAllActivePTUsers();
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/{ptUserId}/gym-associations")
    public ResponseEntity<List<GymPTAssociationResponse>> getPTGymAssociations(@PathVariable Long ptUserId) {
        List<GymPTAssociationResponse> response = associationService.getAssociationsByPTUserId(ptUserId);
        return ResponseEntity.ok(response);
    }
}

