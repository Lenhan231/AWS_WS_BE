package com.easybody.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {

    private Long offerId;
    private Long reportedUserId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String details;
}

