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
