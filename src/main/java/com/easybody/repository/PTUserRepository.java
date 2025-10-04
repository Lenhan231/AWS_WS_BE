package com.easybody.repository;

import com.easybody.model.entity.PTUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PTUserRepository extends JpaRepository<PTUser, Long> {

    Optional<PTUser> findByUserId(Long userId);

    Optional<PTUser> findByIdAndActiveTrue(Long id);

    List<PTUser> findByActiveTrue();

    @Query(value = "SELECT pt.* FROM pt_users pt " +
           "JOIN locations l ON pt.location_id = l.id " +
           "WHERE pt.active = true AND " +
           "ST_DWithin(l.coordinates::geography, " +
           "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, " +
           ":radiusMeters)",
           nativeQuery = true)
    List<PTUser> findPTUsersNearLocation(@Param("latitude") Double latitude,
                                          @Param("longitude") Double longitude,
                                          @Param("radiusMeters") Double radiusMeters);
}

