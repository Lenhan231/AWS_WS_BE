package com.easybody.controller;

import com.easybody.dto.response.PresignedUrlResponse;
import com.easybody.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final S3Service s3Service;

    @GetMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @RequestParam String folder,
            @RequestParam String fileExtension) {

        log.info("Generating presigned URL for folder: {}, extension: {}", folder, fileExtension);

        if (!isValidFolder(folder)) {
            throw new IllegalArgumentException("Invalid folder. Allowed: profiles, offers, gyms");
        }
        if (!isValidFileExtension(fileExtension)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: jpg, jpeg, png, gif, webp");
        }

        PresignedUrlResponse response = s3Service.generatePresignedUploadUrl(folder, fileExtension);
        return ResponseEntity.ok(response);
    }

    private boolean isValidFolder(String folder) {
        return folder.matches("^(profiles|offers|gyms)$");
    }

    private boolean isValidFileExtension(String extension) {
        return extension.matches("^(jpg|jpeg|png|gif|webp)$");
    }
}
