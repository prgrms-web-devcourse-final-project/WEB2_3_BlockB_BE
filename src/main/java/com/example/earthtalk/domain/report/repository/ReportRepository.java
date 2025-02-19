package com.example.earthtalk.domain.report.repository;

import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM reports r JOIN r.targetUser u " +
            "WHERE (:q IS NULL OR u.nickname LIKE CONCAT('%', :q, '%')) AND " +
            "(:reportType IS NULL OR r.reportType = :reportType) AND " +
            "(:resultType IS NULL OR r.resultType = :resultType)")
    Page<Report> getReportsByParams(@Param("q") String q,
                                    @Param("reportType") ReportType reportType,
                                    @Param("resultType")ResultType resultType,
                                    Pageable pageable);
}
