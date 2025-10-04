package com.easybody.repository;

import com.easybody.model.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByOfferId(Long offerId);

    Page<Rating> findByOfferId(Long offerId, Pageable pageable);

    List<Rating> findByClientUserId(Long clientUserId);

    Optional<Rating> findByOfferIdAndClientUserId(Long offerId, Long clientUserId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.offer.id = :offerId")
    Double calculateAverageRating(@Param("offerId") Long offerId);

    Long countByOfferId(Long offerId);
}

