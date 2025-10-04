package com.easybody.dto.request;

import com.easybody.model.enums.OfferStatus;
import com.easybody.model.enums.OfferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferSearchRequest {

    private Double latitude;
    private Double longitude;
    private Double radiusKm;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private OfferType offerType;
    private OfferStatus status;

    private BigDecimal minRating;

    private String searchQuery;

    private Long gymId;
    private Long ptUserId;

    private Boolean active;

    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}

