package com.easybody.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymRegistrationRequest {

    @NotBlank(message = "Gym name is required")
    private String name;

    private String description;
    private String logoUrl;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String country;
    private String postalCode;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String email;
    private String website;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}

