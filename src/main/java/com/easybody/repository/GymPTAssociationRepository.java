package com.easybody.repository;

import com.easybody.model.entity.GymPTAssociation;
import com.easybody.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymPTAssociationRepository extends JpaRepository<GymPTAssociation, Long> {

    List<GymPTAssociation> findByGymId(Long gymId);

    List<GymPTAssociation> findByPtUserId(Long ptUserId);

    List<GymPTAssociation> findByGymIdAndApprovalStatus(Long gymId, ApprovalStatus status);

    List<GymPTAssociation> findByPtUserIdAndApprovalStatus(Long ptUserId, ApprovalStatus status);

    Optional<GymPTAssociation> findByGymIdAndPtUserId(Long gymId, Long ptUserId);

    List<GymPTAssociation> findByApprovalStatus(ApprovalStatus status);
}

