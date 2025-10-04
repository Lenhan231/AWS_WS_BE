package com.easybody.dto.response;

import com.easybody.model.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymPTAssociationResponse {

    private Long id;
    private Long gymId;
    private String gymName;
    private Long ptUserId;
    private String ptUserName;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}

