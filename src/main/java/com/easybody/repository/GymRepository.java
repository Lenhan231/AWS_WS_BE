package com.easybody.repository;

import com.easybody.model.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {

    Optional<Gym> findByIdAndActiveTrue(Long id);

    List<Gym> findByActiveTrue();

    @Query("SELECT g FROM Gym g WHERE g.active = true AND " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(g.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Gym> searchGyms(@Param("searchTerm") String searchTerm);

    @Query(value = "SELECT g.* FROM gyms g " +
           "JOIN locations l ON g.location_id = l.id " +
           "WHERE g.active = true AND " +
           "ST_DWithin(l.coordinates::geography, " +
           "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, " +
           ":radiusMeters)",
           nativeQuery = true)
    List<Gym> findGymsNearLocation(@Param("latitude") Double latitude,
                                    @Param("longitude") Double longitude,
                                    @Param("radiusMeters") Double radiusMeters);
}

