package com.easybody.service;

import com.easybody.dto.request.GymRegistrationRequest;
import com.easybody.dto.request.GymUpdateRequest;
import com.easybody.dto.response.GymResponse;
import com.easybody.dto.response.LocationResponse;
import com.easybody.exception.ResourceNotFoundException;
import com.easybody.model.entity.Gym;
import com.easybody.model.entity.Location;
import com.easybody.repository.GymRepository;
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
public class GymService {

    private final GymRepository gymRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public GymResponse registerGym(GymRegistrationRequest request) {
        log.info("Registering gym: {}", request.getName());

        Location location = createLocation(request.getLatitude(), request.getLongitude());

        Gym gym = Gym.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .website(request.getWebsite())
                .location(location)
                .active(true)
                .verified(false)
                .build();

        gym = gymRepository.save(gym);
        log.info("Gym registered successfully with id: {}", gym.getId());

        return mapToResponse(gym);
    }

    @Transactional
    public GymResponse updateGym(Long gymId, GymUpdateRequest request) {
        log.info("Updating gym with id: {}", gymId);

        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found with id: " + gymId));

        gym.setName(request.getName());
        gym.setDescription(request.getDescription());
        gym.setLogoUrl(request.getLogoUrl());
        gym.setAddress(request.getAddress());
        gym.setCity(request.getCity());
        gym.setState(request.getState());
        gym.setCountry(request.getCountry());
        gym.setPostalCode(request.getPostalCode());
        gym.setPhoneNumber(request.getPhoneNumber());
        gym.setEmail(request.getEmail());
        gym.setWebsite(request.getWebsite());

        if (request.getActive() != null) {
            gym.setActive(request.getActive());
        }

        // Update location if coordinates changed
        if (gym.getLocation() != null) {
            gym.getLocation().setLatitude(request.getLatitude());
            gym.getLocation().setLongitude(request.getLongitude());
            gym.getLocation().setCoordinates(createPoint(request.getLatitude(), request.getLongitude()));
        }

        gym = gymRepository.save(gym);
        log.info("Gym updated successfully");

        return mapToResponse(gym);
    }

    public GymResponse getGymById(Long id) {
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found with id: " + id));
        return mapToResponse(gym);
    }

    public List<GymResponse> getAllActiveGyms() {
        return gymRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GymResponse> searchGyms(String searchTerm) {
        return gymRepository.searchGyms(searchTerm).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GymResponse> findGymsNearLocation(Double latitude, Double longitude, Double radiusKm) {
        Double radiusMeters = radiusKm * 1000;
        return gymRepository.findGymsNearLocation(latitude, longitude, radiusMeters).stream()
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

    private GymResponse mapToResponse(Gym gym) {
        return GymResponse.builder()
                .id(gym.getId())
                .name(gym.getName())
                .description(gym.getDescription())
                .logoUrl(gym.getLogoUrl())
                .address(gym.getAddress())
                .city(gym.getCity())
                .state(gym.getState())
                .country(gym.getCountry())
                .postalCode(gym.getPostalCode())
                .phoneNumber(gym.getPhoneNumber())
                .email(gym.getEmail())
                .website(gym.getWebsite())
                .location(gym.getLocation() != null ? mapLocationToResponse(gym.getLocation()) : null)
                .active(gym.getActive())
                .verified(gym.getVerified())
                .createdAt(gym.getCreatedAt())
                .updatedAt(gym.getUpdatedAt())
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

