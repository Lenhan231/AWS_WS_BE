package com.easybody.dto.response;

import com.easybody.model.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long id;
    private Long reportedByUserId;
    private String reportedByUserName;
    private Long offerId;
    private String offerTitle;
    private Long reportedUserId;
    private String reportedUserName;
    private String reason;
    private String details;
    private ReportStatus status;
    private String reviewNotes;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}

