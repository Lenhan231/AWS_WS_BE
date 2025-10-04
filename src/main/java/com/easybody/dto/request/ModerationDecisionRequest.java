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
public class ModerationDecisionRequest {

    @NotBlank(message = "Decision is required (approve/reject)")
    private String decision;

    private String reason;
}

