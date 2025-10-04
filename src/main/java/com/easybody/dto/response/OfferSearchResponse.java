package com.easybody.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferSearchResponse {

    private Long id;
    private String title;
    private String description;
    private String offerType;
    private String gymName;
    private String ptUserName;
    private BigDecimal price;
    private String currency;
    private String durationDescription;
    private String imageUrls;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Double distanceKm;
    private LocationResponse location;
    private LocalDateTime createdAt;
}
package com.easybody.dto.response;

import com.easybody.model.enums.OfferStatus;
import com.easybody.model.enums.OfferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {

    private Long id;
    private String title;
    private String description;
    private OfferType offerType;
    private Long gymId;
    private String gymName;
    private Long ptUserId;
    private String ptUserName;
    private BigDecimal price;
    private String currency;
    private String durationDescription;
    private String imageUrls;
    private OfferStatus status;
    private BigDecimal riskScore;
    private String rejectionReason;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime moderatedAt;
}

