package com.easybody.controller;

import com.easybody.dto.request.ModerationDecisionRequest;
import com.easybody.dto.response.GymPTAssociationResponse;
import com.easybody.dto.response.OfferResponse;
import com.easybody.dto.response.PageResponse;
import com.easybody.dto.response.ReportResponse;
import com.easybody.model.enums.ReportStatus;
import com.easybody.service.GymPTAssociationService;
import com.easybody.service.OfferService;
import com.easybody.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final OfferService offerService;
    private final ReportService reportService;
    private final GymPTAssociationService associationService;

    // Offer Moderation
    @GetMapping("/offers/pending")
    public ResponseEntity<PageResponse<OfferResponse>> getPendingOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<OfferResponse> response = offerService.getPendingOffers(page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/offers/{offerId}/moderate")
    public ResponseEntity<OfferResponse> moderateOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody ModerationDecisionRequest request,
            Authentication authentication) {

        OfferResponse response;
        if ("approve".equalsIgnoreCase(request.getDecision())) {
            response = offerService.approveOffer(offerId, authentication.getName());
        } else if ("reject".equalsIgnoreCase(request.getDecision())) {
            response = offerService.rejectOffer(offerId, request.getReason(), authentication.getName());
        } else {
            throw new IllegalArgumentException("Invalid decision. Must be 'approve' or 'reject'");
        }

        return ResponseEntity.ok(response);
    }

    // Report Management
    @GetMapping("/reports/pending")
    public ResponseEntity<PageResponse<ReportResponse>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<ReportResponse> response = reportService.getPendingReports(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports")
    public ResponseEntity<PageResponse<ReportResponse>> getReportsByStatus(
            @RequestParam ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<ReportResponse> response = reportService.getReportsByStatus(status, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long reportId,
            @RequestParam(required = false) String reviewNotes,
            Authentication authentication) {

        ReportResponse response = reportService.resolveReport(reportId, reviewNotes, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reports/{reportId}/dismiss")
    public ResponseEntity<ReportResponse> dismissReport(
            @PathVariable Long reportId,
            @RequestParam(required = false) String reviewNotes,
            Authentication authentication) {

        ReportResponse response = reportService.dismissReport(reportId, reviewNotes, authentication.getName());
        return ResponseEntity.ok(response);
    }

    // PT-Gym Association Management
    @GetMapping("/pt-associations/pending")
    public ResponseEntity<List<GymPTAssociationResponse>> getPendingAssociations() {
        List<GymPTAssociationResponse> response = associationService.getPendingAssociations();
        return ResponseEntity.ok(response);
    }
}

