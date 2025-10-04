package com.easybody.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignPTToGymRequest {

    @NotNull(message = "PT User ID is required")
    private Long ptUserId;

    @NotNull(message = "Gym ID is required")
    private Long gymId;
}

