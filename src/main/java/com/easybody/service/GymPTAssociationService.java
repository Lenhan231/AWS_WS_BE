package com.easybody.service;

import com.easybody.dto.request.AssignPTToGymRequest;
import com.easybody.dto.response.GymPTAssociationResponse;
import com.easybody.exception.ResourceNotFoundException;
import com.easybody.model.entity.Gym;
import com.easybody.model.entity.GymPTAssociation;
import com.easybody.model.entity.PTUser;
import com.easybody.model.entity.User;
import com.easybody.model.enums.ApprovalStatus;
import com.easybody.repository.GymPTAssociationRepository;
import com.easybody.repository.GymRepository;
import com.easybody.repository.PTUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GymPTAssociationService {

    private final GymPTAssociationRepository associationRepository;
    private final GymRepository gymRepository;
    private final PTUserRepository ptUserRepository;
    private final UserService userService;

    @Transactional
    public GymPTAssociationResponse assignPTToGym(AssignPTToGymRequest request) {
        log.info("Assigning PT {} to Gym {}", request.getPtUserId(), request.getGymId());

        Gym gym = gymRepository.findById(request.getGymId())
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        PTUser ptUser = ptUserRepository.findById(request.getPtUserId())
                .orElseThrow(() -> new ResourceNotFoundException("PT User not found"));

        // Check if association already exists
        associationRepository.findByGymIdAndPtUserId(request.getGymId(), request.getPtUserId())
                .ifPresent(a -> {
                    throw new IllegalArgumentException("PT is already associated with this gym");
                });

        GymPTAssociation association = GymPTAssociation.builder()
                .gym(gym)
                .ptUser(ptUser)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        association = associationRepository.save(association);
        log.info("PT assigned to gym successfully, association id: {}", association.getId());

        return mapToResponse(association);
    }

    @Transactional
    public GymPTAssociationResponse approveAssociation(Long associationId, String cognitoSub) {
        log.info("Approving association id: {}", associationId);

        GymPTAssociation association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));

        User approver = userService.getUserEntityByCognitoSub(cognitoSub);

        association.setApprovalStatus(ApprovalStatus.APPROVED);
        association.setApprovedBy(approver);
        association.setApprovedAt(LocalDateTime.now());

        association = associationRepository.save(association);
        log.info("Association approved successfully");

        return mapToResponse(association);
    }

    @Transactional
    public GymPTAssociationResponse rejectAssociation(Long associationId, String reason, String cognitoSub) {
        log.info("Rejecting association id: {}", associationId);

        GymPTAssociation association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));

        User approver = userService.getUserEntityByCognitoSub(cognitoSub);

        association.setApprovalStatus(ApprovalStatus.REJECTED);
        association.setRejectionReason(reason);
        association.setApprovedBy(approver);
        association.setApprovedAt(LocalDateTime.now());

        association = associationRepository.save(association);
        log.info("Association rejected successfully");

        return mapToResponse(association);
    }

    public List<GymPTAssociationResponse> getAssociationsByGymId(Long gymId) {
        return associationRepository.findByGymId(gymId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GymPTAssociationResponse> getAssociationsByPTUserId(Long ptUserId) {
        return associationRepository.findByPtUserId(ptUserId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GymPTAssociationResponse> getPendingAssociations() {
        return associationRepository.findByApprovalStatus(ApprovalStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private GymPTAssociationResponse mapToResponse(GymPTAssociation association) {
        return GymPTAssociationResponse.builder()
                .id(association.getId())
                .gymId(association.getGym().getId())
                .gymName(association.getGym().getName())
                .ptUserId(association.getPtUser().getId())
                .ptUserName(association.getPtUser().getUser().getFirstName() + " " +
                           association.getPtUser().getUser().getLastName())
                .approvalStatus(association.getApprovalStatus())
                .rejectionReason(association.getRejectionReason())
                .approvedAt(association.getApprovedAt())
                .createdAt(association.getCreatedAt())
                .build();
    }
}

