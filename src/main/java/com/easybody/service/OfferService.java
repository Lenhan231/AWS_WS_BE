
    @Transactional
    public OfferResponse updateOffer(Long offerId, OfferUpdateRequest request) {
        log.info("Updating offer with id: {}", offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setPrice(request.getPrice());
        if (request.getCurrency() != null) offer.setCurrency(request.getCurrency());
        offer.setDurationDescription(request.getDurationDescription());
        offer.setImageUrls(request.getImageUrls());
        if (request.getActive() != null) offer.setActive(request.getActive());

        // Reset to pending if content changed
        offer.setStatus(OfferStatus.PENDING);

        offer = offerRepository.save(offer);
        log.info("Offer updated successfully");

        return mapToResponse(offer);
    }

    @Transactional
    public OfferResponse approveOffer(Long offerId, String cognitoSub) {
        log.info("Approving offer with id: {}", offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        User moderator = userService.getUserEntityByCognitoSub(cognitoSub);

        offer.setStatus(OfferStatus.APPROVED);
        offer.setModeratedBy(moderator);
        offer.setModeratedAt(LocalDateTime.now());

        offer = offerRepository.save(offer);
        log.info("Offer approved successfully");

        return mapToResponse(offer);
    }

    @Transactional
    public OfferResponse rejectOffer(Long offerId, String reason, String cognitoSub) {
        log.info("Rejecting offer with id: {}", offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        User moderator = userService.getUserEntityByCognitoSub(cognitoSub);

        offer.setStatus(OfferStatus.REJECTED);
        offer.setRejectionReason(reason);
        offer.setModeratedBy(moderator);
        offer.setModeratedAt(LocalDateTime.now());

        offer = offerRepository.save(offer);
        log.info("Offer rejected successfully");

        return mapToResponse(offer);
    }

    public OfferResponse getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        return mapToResponse(offer);
    }

    public PageResponse<OfferResponse> getPendingOffers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Offer> offerPage = offerRepository.findByStatus(OfferStatus.PENDING, pageable);
        return mapToPageResponse(offerPage);
    }

    public PageResponse<OfferSearchResponse> searchOffers(OfferSearchRequest request) {
        log.info("Searching offers with filters");

        Specification<Offer> spec = buildSpecification(request);

        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Offer> offerPage = offerRepository.findAll(spec, pageable);

        return mapToSearchPageResponse(offerPage);
    }

    private Specification<Offer> buildSpecification(OfferSearchRequest request) {
        Specification<Offer> spec = Specification.where(null);

        // Active filter
        if (request.getActive() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), request.getActive()));
        } else {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));
        }

        // Status filter - default to APPROVED for public search
        if (request.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), request.getStatus()));
        } else {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), OfferStatus.APPROVED));
        }

        // Offer type filter
        if (request.getOfferType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("offerType"), request.getOfferType()));
        }

        // Price range filter
        if (request.getMinPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
        }
        if (request.getMaxPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
        }

        // Rating filter
        if (request.getMinRating() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("averageRating"), request.getMinRating()));
        }

        // Gym filter
        if (request.getGymId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("gym").get("id"), request.getGymId()));
        }

        // PT User filter
        if (request.getPtUserId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ptUser").get("id"), request.getPtUserId()));
        }

        // Text search filter (title and description)
        if (request.getSearchQuery() != null && !request.getSearchQuery().isBlank()) {
            String searchPattern = "%" + request.getSearchQuery().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
            ));
        }

        return spec;
    }

    private PageResponse<OfferResponse> mapToPageResponse(Page<Offer> page) {
        return PageResponse.<OfferResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private PageResponse<OfferSearchResponse> mapToSearchPageResponse(Page<Offer> page) {
        return PageResponse.<OfferSearchResponse>builder()
                .content(page.getContent().stream().map(this::mapToSearchResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private OfferResponse mapToResponse(Offer offer) {
        return OfferResponse.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .offerType(offer.getOfferType())
                .gymId(offer.getGym() != null ? offer.getGym().getId() : null)
                .gymName(offer.getGym() != null ? offer.getGym().getName() : null)
                .ptUserId(offer.getPtUser() != null ? offer.getPtUser().getId() : null)
                .ptUserName(offer.getPtUser() != null ?
                           offer.getPtUser().getUser().getFirstName() + " " +
                           offer.getPtUser().getUser().getLastName() : null)
                .price(offer.getPrice())
                .currency(offer.getCurrency())
                .durationDescription(offer.getDurationDescription())
                .imageUrls(offer.getImageUrls())
                .status(offer.getStatus())
                .riskScore(offer.getRiskScore())
                .rejectionReason(offer.getRejectionReason())
                .averageRating(offer.getAverageRating())
                .ratingCount(offer.getRatingCount())
                .active(offer.getActive())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .moderatedAt(offer.getModeratedAt())
                .build();
    }

    private OfferSearchResponse mapToSearchResponse(Offer offer) {
        return OfferSearchResponse.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .offerType(offer.getOfferType().name())
                .gymName(offer.getGym() != null ? offer.getGym().getName() : null)
                .ptUserName(offer.getPtUser() != null ?
                           offer.getPtUser().getUser().getFirstName() + " " +
                           offer.getPtUser().getUser().getLastName() : null)
                .price(offer.getPrice())
                .currency(offer.getCurrency())
                .durationDescription(offer.getDurationDescription())
                .imageUrls(offer.getImageUrls())
                .averageRating(offer.getAverageRating())
                .ratingCount(offer.getRatingCount())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
package com.easybody.service;

import com.easybody.dto.request.OfferCreateRequest;
import com.easybody.dto.request.OfferSearchRequest;
import com.easybody.dto.request.OfferUpdateRequest;
import com.easybody.dto.response.OfferResponse;
import com.easybody.dto.response.OfferSearchResponse;
import com.easybody.dto.response.PageResponse;
import com.easybody.exception.ResourceNotFoundException;
import com.easybody.model.entity.Gym;
import com.easybody.model.entity.Offer;
import com.easybody.model.entity.PTUser;
import com.easybody.model.entity.User;
import com.easybody.model.enums.OfferStatus;
import com.easybody.model.enums.OfferType;
import com.easybody.repository.GymRepository;
import com.easybody.repository.OfferRepository;
import com.easybody.repository.PTUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final GymRepository gymRepository;
    private final PTUserRepository ptUserRepository;
    private final UserService userService;

    @Transactional
    public OfferResponse createOffer(OfferCreateRequest request, String cognitoSub) {
        log.info("Creating offer: {}", request.getTitle());

        Gym gym = null;
        PTUser ptUser = null;

        if (request.getOfferType() == OfferType.GYM_OFFER) {
            if (request.getGymId() == null) {
                throw new IllegalArgumentException("Gym ID is required for gym offers");
            }
            gym = gymRepository.findById(request.getGymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));
        } else if (request.getOfferType() == OfferType.PT_OFFER) {
            if (request.getPtUserId() == null) {
                throw new IllegalArgumentException("PT User ID is required for PT offers");
            }
            ptUser = ptUserRepository.findById(request.getPtUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("PT User not found"));
        }

        Offer offer = Offer.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .offerType(request.getOfferType())
                .gym(gym)
                .ptUser(ptUser)
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .durationDescription(request.getDurationDescription())
                .imageUrls(request.getImageUrls())
                .status(OfferStatus.PENDING)
                .riskScore(BigDecimal.ZERO)
                .active(true)
                .averageRating(BigDecimal.ZERO)
                .ratingCount(0)
                .build();

        offer = offerRepository.save(offer);
        log.info("Offer created successfully with id: {}", offer.getId());

        // TODO: Send to SQS queue for image moderation

        return mapToResponse(offer);
    }

