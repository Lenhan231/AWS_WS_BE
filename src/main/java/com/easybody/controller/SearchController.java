package com.easybody.controller;

import com.easybody.dto.request.OfferSearchRequest;
import com.easybody.dto.response.OfferSearchResponse;
import com.easybody.dto.response.PageResponse;
import com.easybody.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final OfferService offerService;

    @PostMapping("/offers")
    public ResponseEntity<PageResponse<OfferSearchResponse>> searchOffers(
            @RequestBody OfferSearchRequest request) {

        log.info("Searching offers with filters: {}", request);
        PageResponse<OfferSearchResponse> response = offerService.searchOffers(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/offers")
    public ResponseEntity<PageResponse<OfferSearchResponse>> searchOffersGet(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10") Double radiusKm,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String offerType,
            @RequestParam(required = false) String minRating,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Long gymId,
            @RequestParam(required = false) Long ptUserId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        OfferSearchRequest request = OfferSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radiusKm(radiusKm)
                .minPrice(minPrice != null ? new java.math.BigDecimal(minPrice) : null)
                .maxPrice(maxPrice != null ? new java.math.BigDecimal(maxPrice) : null)
                .offerType(offerType != null ? com.easybody.model.enums.OfferType.valueOf(offerType) : null)
                .minRating(minRating != null ? new java.math.BigDecimal(minRating) : null)
                .searchQuery(searchQuery)
                .gymId(gymId)
                .ptUserId(ptUserId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PageResponse<OfferSearchResponse> response = offerService.searchOffers(request);
        return ResponseEntity.ok(response);
    }
}

