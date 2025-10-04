package com.easybody.repository;

import com.easybody.model.entity.Report;
import com.easybody.model.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatus(ReportStatus status);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    List<Report> findByReportedById(Long userId);

    List<Report> findByOfferId(Long offerId);

    List<Report> findByReportedUserId(Long userId);
}

