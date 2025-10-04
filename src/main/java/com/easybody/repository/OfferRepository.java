package com.easybody.repository;

import com.easybody.model.entity.Offer;
import com.easybody.model.enums.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {

    Optional<Offer> findByIdAndActiveTrue(Long id);

    List<Offer> findByStatus(OfferStatus status);

    Page<Offer> findByStatus(OfferStatus status, Pageable pageable);

    List<Offer> findByGymId(Long gymId);

    List<Offer> findByPtUserId(Long ptUserId);

    Page<Offer> findByGymIdAndActiveTrue(Long gymId, Pageable pageable);

    Page<Offer> findByPtUserIdAndActiveTrue(Long ptUserId, Pageable pageable);

    Page<Offer> findByStatusAndActiveTrue(OfferStatus status, Pageable pageable);

    @Query(value = "SELECT o.*, " +
           "ST_Distance(l.coordinates::geography, " +
           "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) / 1000 as distance_km " +
           "FROM offers o " +
           "LEFT JOIN gyms g ON o.gym_id = g.id " +
           "LEFT JOIN pt_users pt ON o.pt_user_id = pt.id " +
           "LEFT JOIN locations l ON (g.location_id = l.id OR pt.location_id = l.id) " +
           "WHERE o.active = true AND o.status = 'APPROVED' " +
           "AND ST_DWithin(l.coordinates::geography, " +
           "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, " +
           ":radiusMeters) " +
           "ORDER BY distance_km",
           nativeQuery = true)
    List<Offer> findOffersNearLocation(@Param("latitude") Double latitude,
                                        @Param("longitude") Double longitude,
                                        @Param("radiusMeters") Double radiusMeters);
}

