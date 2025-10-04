package com.easybody.repository;

import com.easybody.model.entity.GymStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymStaffRepository extends JpaRepository<GymStaff, Long> {

    List<GymStaff> findByGymId(Long gymId);

    List<GymStaff> findByUserId(Long userId);

    Optional<GymStaff> findByUserIdAndGymId(Long userId, Long gymId);

    List<GymStaff> findByGymIdAndActiveTrue(Long gymId);
}

