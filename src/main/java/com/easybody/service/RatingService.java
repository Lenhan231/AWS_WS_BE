package com.easybody.service;

import com.easybody.dto.request.RatingCreateRequest;
import com.easybody.dto.response.PageResponse;
import com.easybody.dto.response.RatingResponse;
import com.easybody.exception.ResourceNotFoundException;
import com.easybody.model.entity.Offer;
import com.easybody.model.entity.Rating;
import com.easybody.model.entity.User;
import com.easybody.repository.OfferRepository;
import com.easybody.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final OfferRepository offerRepository;
    private final UserService userService;

    @Transactional
    public RatingResponse createRating(RatingCreateRequest request, String cognitoSub) {
        log.info("Creating rating for offer: {}", request.getOfferId());

        User clientUser = userService.getUserEntityByCognitoSub(cognitoSub);

        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        // Check if user already rated this offer
        ratingRepository.findByOfferIdAndClientUserId(request.getOfferId(), clientUser.getId())
                .ifPresent(r -> {
                    throw new IllegalArgumentException("You have already rated this offer");
                });

        Rating rating = Rating.builder()
                .offer(offer)
                .clientUser(clientUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        rating = ratingRepository.save(rating);
        log.info("Rating created successfully with id: {}", rating.getId());

        // Update offer average rating
        updateOfferRating(offer.getId());

        return mapToResponse(rating);
    }

    @Transactional
    public void updateOfferRating(Long offerId) {
        Double avgRating = ratingRepository.calculateAverageRating(offerId);
        Long ratingCount = ratingRepository.countByOfferId(offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        offer.setAverageRating(avgRating != null ?
                BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        offer.setRatingCount(ratingCount.intValue());

        offerRepository.save(offer);
        log.info("Updated offer {} rating: {} ({} ratings)", offerId, avgRating, ratingCount);
    }

    public PageResponse<RatingResponse> getRatingsByOfferId(Long offerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Rating> ratingPage = ratingRepository.findByOfferId(offerId, pageable);
        return mapToPageResponse(ratingPage);
    }

    private PageResponse<RatingResponse> mapToPageResponse(Page<Rating> page) {
        return PageResponse.<RatingResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .offerId(rating.getOffer().getId())
                .offerTitle(rating.getOffer().getTitle())
                .clientUserId(rating.getClientUser().getId())
                .clientUserName(rating.getClientUser().getFirstName() + " " +
                               rating.getClientUser().getLastName())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}

