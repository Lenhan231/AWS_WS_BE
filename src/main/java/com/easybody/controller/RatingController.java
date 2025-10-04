package com.easybody.controller;

import com.easybody.dto.request.RatingCreateRequest;
import com.easybody.dto.response.PageResponse;
import com.easybody.dto.response.RatingResponse;
import com.easybody.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENT_USER')")
    public ResponseEntity<RatingResponse> createRating(
            @Valid @RequestBody RatingCreateRequest request,
            Authentication authentication) {

        RatingResponse response = ratingService.createRating(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/offer/{offerId}")
    public ResponseEntity<PageResponse<RatingResponse>> getRatingsByOfferId(
            @PathVariable Long offerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<RatingResponse> response = ratingService.getRatingsByOfferId(offerId, page, size);
        return ResponseEntity.ok(response);
    }
}

