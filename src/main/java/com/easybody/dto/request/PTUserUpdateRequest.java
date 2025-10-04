package com.easybody.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PTUserUpdateRequest {

    private String bio;
    private String specializations;
    private String certifications;
    private Integer yearsOfExperience;
    private String profileImageUrl;
    private Double latitude;
    private Double longitude;
    private Boolean active;
}

