package com.easybody.controller;

import com.easybody.dto.request.AssignPTToGymRequest;
import com.easybody.dto.request.GymRegistrationRequest;
import com.easybody.dto.request.GymUpdateRequest;
import com.easybody.dto.response.GymPTAssociationResponse;
import com.easybody.dto.response.GymResponse;
import com.easybody.service.GymPTAssociationService;
import com.easybody.service.GymService;
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
@RequestMapping("/api/v1/gyms")
@RequiredArgsConstructor
@Slf4j
public class GymController {

    private final GymService gymService;
    private final GymPTAssociationService associationService;

    @PostMapping
    @PreAuthorize("hasAuthority('GYM_STAFF') or hasAuthority('ADMIN')")
    public ResponseEntity<GymResponse> registerGym(@Valid @RequestBody GymRegistrationRequest request) {
        GymResponse response = gymService.registerGym(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{gymId}")
    @PreAuthorize("hasAuthority('GYM_STAFF') or hasAuthority('ADMIN')")
    public ResponseEntity<GymResponse> updateGym(
            @PathVariable Long gymId,
            @Valid @RequestBody GymUpdateRequest request) {
        GymResponse response = gymService.updateGym(gymId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gymId}")
    public ResponseEntity<GymResponse> getGymById(@PathVariable Long gymId) {
        GymResponse response = gymService.getGymById(gymId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<GymResponse>> getAllGyms() {
        List<GymResponse> response = gymService.getAllActiveGyms();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GymResponse>> searchGyms(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10") Double radiusKm) {

        if (latitude != null && longitude != null) {
            List<GymResponse> response = gymService.findGymsNearLocation(latitude, longitude, radiusKm);
            return ResponseEntity.ok(response);
        } else if (query != null) {
            List<GymResponse> response = gymService.searchGyms(query);
            return ResponseEntity.ok(response);
        } else {
            List<GymResponse> response = gymService.getAllActiveGyms();
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/{gymId}/assign-pt")
    @PreAuthorize("hasAuthority('GYM_STAFF') or hasAuthority('ADMIN')")
    public ResponseEntity<GymPTAssociationResponse> assignPTToGym(
            @PathVariable Long gymId,
            @RequestParam Long ptUserId) {

        AssignPTToGymRequest request = AssignPTToGymRequest.builder()
                .gymId(gymId)
                .ptUserId(ptUserId)
                .build();

        GymPTAssociationResponse response = associationService.assignPTToGym(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{gymId}/pt-associations")
    public ResponseEntity<List<GymPTAssociationResponse>> getGymPTAssociations(@PathVariable Long gymId) {
        List<GymPTAssociationResponse> response = associationService.getAssociationsByGymId(gymId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/pt-associations/{associationId}/approve")
    @PreAuthorize("hasAuthority('GYM_STAFF') or hasAuthority('ADMIN')")
    public ResponseEntity<GymPTAssociationResponse> approveAssociation(
            @PathVariable Long associationId,
            Authentication authentication) {

        GymPTAssociationResponse response = associationService.approveAssociation(
                associationId,
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/pt-associations/{associationId}/reject")
    @PreAuthorize("hasAuthority('GYM_STAFF') or hasAuthority('ADMIN')")
    public ResponseEntity<GymPTAssociationResponse> rejectAssociation(
            @PathVariable Long associationId,
            @RequestParam String reason,
            Authentication authentication) {

        GymPTAssociationResponse response = associationService.rejectAssociation(
                associationId,
                reason,
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }
}

