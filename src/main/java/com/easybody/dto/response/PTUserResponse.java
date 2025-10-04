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
public class PTUserResponse {

    private Long id;
    private UserResponse user;
    private String bio;
    private String specializations;
    private String certifications;
    private Integer yearsOfExperience;
    private String profileImageUrl;
    private LocationResponse location;
    private Boolean active;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

