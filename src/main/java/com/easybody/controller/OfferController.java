package com.easybody.controller;

import com.easybody.dto.request.OfferCreateRequest;
import com.easybody.dto.request.OfferUpdateRequest;
import com.easybody.dto.response.OfferResponse;
import com.easybody.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
@Slf4j
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    @PreAuthorize("hasAuthority('GYM_STAFF') or hasAuthority('PT_USER') or hasAuthority('ADMIN')")
    public ResponseEntity<OfferResponse> createOffer(
            @Valid @RequestBody OfferCreateRequest request,
            Authentication authentication) {

        OfferResponse response = offerService.createOffer(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{offerId}")
    @PreAuthorize("hasAuthority('GYM_STAFF') or hasAuthority('PT_USER') or hasAuthority('ADMIN')")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody OfferUpdateRequest request) {

        OfferResponse response = offerService.updateOffer(offerId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<OfferResponse> getOfferById(@PathVariable Long offerId) {
        OfferResponse response = offerService.getOfferById(offerId);
        return ResponseEntity.ok(response);
    }
}

