package com.easybody.controller;

import com.easybody.dto.request.ReportCreateRequest;
import com.easybody.dto.response.ReportResponse;
import com.easybody.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportCreateRequest request,
            Authentication authentication) {

        ReportResponse response = reportService.createReport(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

