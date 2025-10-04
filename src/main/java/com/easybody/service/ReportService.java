package com.easybody.service;

import com.easybody.dto.request.ReportCreateRequest;
import com.easybody.dto.response.PageResponse;
import com.easybody.dto.response.ReportResponse;
import com.easybody.exception.ResourceNotFoundException;
import com.easybody.model.entity.Offer;
import com.easybody.model.entity.Report;
import com.easybody.model.entity.User;
import com.easybody.model.enums.ReportStatus;
import com.easybody.repository.OfferRepository;
import com.easybody.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final OfferRepository offerRepository;
    private final UserService userService;

    @Transactional
    public ReportResponse createReport(ReportCreateRequest request, String cognitoSub) {
        log.info("Creating report");

        User reportedBy = userService.getUserEntityByCognitoSub(cognitoSub);

        Offer offer = null;
        User reportedUser = null;

        if (request.getOfferId() != null) {
            offer = offerRepository.findById(request.getOfferId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
        }

        if (request.getReportedUserId() != null) {
            reportedUser = userService.getUserEntityById(request.getReportedUserId());
        }

        if (offer == null && reportedUser == null) {
            throw new IllegalArgumentException("Either offerId or reportedUserId must be provided");
        }

        Report report = Report.builder()
                .reportedBy(reportedBy)
                .offer(offer)
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .details(request.getDetails())
                .status(ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        log.info("Report created successfully with id: {}", report.getId());

        return mapToResponse(report);
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId, String reviewNotes, String cognitoSub) {
        log.info("Resolving report with id: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User reviewer = userService.getUserEntityByCognitoSub(cognitoSub);

        report.setStatus(ReportStatus.RESOLVED);
        report.setReviewedBy(reviewer);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewNotes(reviewNotes);

        report = reportRepository.save(report);
        log.info("Report resolved successfully");

        return mapToResponse(report);
    }

    @Transactional
    public ReportResponse dismissReport(Long reportId, String reviewNotes, String cognitoSub) {
        log.info("Dismissing report with id: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User reviewer = userService.getUserEntityByCognitoSub(cognitoSub);

        report.setStatus(ReportStatus.DISMISSED);
        report.setReviewedBy(reviewer);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewNotes(reviewNotes);

        report = reportRepository.save(report);
        log.info("Report dismissed successfully");

        return mapToResponse(report);
    }

    public PageResponse<ReportResponse> getPendingReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Report> reportPage = reportRepository.findByStatus(ReportStatus.PENDING, pageable);
        return mapToPageResponse(reportPage);
    }

    public PageResponse<ReportResponse> getReportsByStatus(ReportStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Report> reportPage = reportRepository.findByStatus(status, pageable);
        return mapToPageResponse(reportPage);
    }

    private PageResponse<ReportResponse> mapToPageResponse(Page<Report> page) {
        return PageResponse.<ReportResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reportedByUserId(report.getReportedBy().getId())
                .reportedByUserName(report.getReportedBy().getFirstName() + " " +
                                   report.getReportedBy().getLastName())
                .offerId(report.getOffer() != null ? report.getOffer().getId() : null)
                .offerTitle(report.getOffer() != null ? report.getOffer().getTitle() : null)
                .reportedUserId(report.getReportedUser() != null ? report.getReportedUser().getId() : null)
                .reportedUserName(report.getReportedUser() != null ?
                                 report.getReportedUser().getFirstName() + " " +
                                 report.getReportedUser().getLastName() : null)
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .reviewNotes(report.getReviewNotes())
                .createdAt(report.getCreatedAt())
                .reviewedAt(report.getReviewedAt())
                .build();
    }
}

