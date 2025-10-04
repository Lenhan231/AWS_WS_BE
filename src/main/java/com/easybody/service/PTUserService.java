package com.easybody.service;

import com.easybody.dto.request.PTUserCreateRequest;
import com.easybody.dto.request.PTUserUpdateRequest;
import com.easybody.dto.response.LocationResponse;
import com.easybody.dto.response.PTUserResponse;
import com.easybody.dto.response.UserResponse;
import com.easybody.exception.ResourceNotFoundException;
import com.easybody.model.entity.Location;
import com.easybody.model.entity.PTUser;
import com.easybody.model.entity.User;
import com.easybody.model.enums.Role;
import com.easybody.repository.PTUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PTUserService {

    private final PTUserRepository ptUserRepository;
    private final UserService userService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public PTUserResponse createPTProfile(String cognitoSub, PTUserCreateRequest request) {
        log.info("Creating PT profile for cognitoSub: {}", cognitoSub);

        User user = userService.getUserEntityByCognitoSub(cognitoSub);

        if (user.getRole() != Role.PT_USER) {
            throw new IllegalArgumentException("User must have PT_USER role");
        }

        Location location = createLocation(request.getLatitude(), request.getLongitude());

        PTUser ptUser = PTUser.builder()
                .user(user)
                .bio(request.getBio())
                .specializations(request.getSpecializations())
                .certifications(request.getCertifications())
                .yearsOfExperience(request.getYearsOfExperience())
                .profileImageUrl(request.getProfileImageUrl())
                .location(location)
                .active(true)
                .verified(false)
                .build();

        ptUser = ptUserRepository.save(ptUser);
        log.info("PT profile created successfully with id: {}", ptUser.getId());

        return mapToResponse(ptUser);
    }

    @Transactional
    public PTUserResponse updatePTProfile(Long ptUserId, PTUserUpdateRequest request) {
        log.info("Updating PT profile with id: {}", ptUserId);

        PTUser ptUser = ptUserRepository.findById(ptUserId)
                .orElseThrow(() -> new ResourceNotFoundException("PT User not found with id: " + ptUserId));

        if (request.getBio() != null) ptUser.setBio(request.getBio());
        if (request.getSpecializations() != null) ptUser.setSpecializations(request.getSpecializations());
        if (request.getCertifications() != null) ptUser.setCertifications(request.getCertifications());
        if (request.getYearsOfExperience() != null) ptUser.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getProfileImageUrl() != null) ptUser.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getActive() != null) ptUser.setActive(request.getActive());

        // Update location if coordinates provided
        if (request.getLatitude() != null && request.getLongitude() != null && ptUser.getLocation() != null) {
            ptUser.getLocation().setLatitude(request.getLatitude());
            ptUser.getLocation().setLongitude(request.getLongitude());
            ptUser.getLocation().setCoordinates(createPoint(request.getLatitude(), request.getLongitude()));
        }

        ptUser = ptUserRepository.save(ptUser);
        log.info("PT profile updated successfully");

        return mapToResponse(ptUser);
    }

    public PTUserResponse getPTUserById(Long id) {
        PTUser ptUser = ptUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PT User not found with id: " + id));
        return mapToResponse(ptUser);
    }

    public List<PTUserResponse> getAllActivePTUsers() {
        return ptUserRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PTUserResponse> findPTUsersNearLocation(Double latitude, Double longitude, Double radiusKm) {
        Double radiusMeters = radiusKm * 1000;
        return ptUserRepository.findPTUsersNearLocation(latitude, longitude, radiusMeters).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Location createLocation(Double latitude, Double longitude) {
        Point point = createPoint(latitude, longitude);
        return Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .coordinates(point)
                .build();
    }

    private Point createPoint(Double latitude, Double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private PTUserResponse mapToResponse(PTUser ptUser) {
        return PTUserResponse.builder()
                .id(ptUser.getId())
                .user(mapUserToResponse(ptUser.getUser()))
                .bio(ptUser.getBio())
                .specializations(ptUser.getSpecializations())
                .certifications(ptUser.getCertifications())
                .yearsOfExperience(ptUser.getYearsOfExperience())
                .profileImageUrl(ptUser.getProfileImageUrl())
                .location(ptUser.getLocation() != null ? mapLocationToResponse(ptUser.getLocation()) : null)
                .active(ptUser.getActive())
                .verified(ptUser.getVerified())
                .createdAt(ptUser.getCreatedAt())
                .updatedAt(ptUser.getUpdatedAt())
                .build();
    }

    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .active(user.getActive())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private LocationResponse mapLocationToResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .formattedAddress(location.getFormattedAddress())
                .build();
    }
}

