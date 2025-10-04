package com.easybody.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private Long id;
    private Long offerId;
    private String offerTitle;
    private Long clientUserId;
    private String clientUserName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

