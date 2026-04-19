package com.barangay.barangay.clearance_management.repository;

import com.barangay.barangay.clearance_management.model.RevenueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RevenueRecordRepository extends JpaRepository<RevenueRecord, Long> {
    @Query("SELECT SUM(r.amount) FROM RevenueRecord r " +
            "JOIN r.issuedCertificate i " +
            "WHERE i.status = 'RELEASED' AND i.isArchive = false")
    BigDecimal sumTotalReleasedRevenue();

    @Query("SELECT SUM(r.amount) FROM RevenueRecord r " +
            "JOIN r.issuedCertificate i " +
            "WHERE i.status = 'RELEASED' AND i.isArchive = false AND r.paymentDate >= :startDate")
    BigDecimal sumReleasedRevenueSince(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT CAST(DATE_TRUNC('day', r.payment_date) AS DATE) as label, SUM(r.amount) as revenue " +
            "FROM revenue_record r " +
            "JOIN issued_certificate i ON r.issued_cert_id = i.id " +
            "WHERE i.status = 'RELEASED' AND i.is_archive = false " +
            "AND r.payment_date BETWEEN :start AND :end " +
            "GROUP BY label ORDER BY label", nativeQuery = true)
    List<Object[]> getDailyRevenueTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT TO_CHAR(r.payment_date, 'YYYY-MM') as label, SUM(r.amount) as revenue " +
            "FROM revenue_record r " +
            "JOIN issued_certificate i ON r.issued_cert_id = i.id " +
            "WHERE i.status = 'RELEASED' AND i.is_archive = false " +
            "AND r.payment_date BETWEEN :start AND :end " +
            "GROUP BY label ORDER BY label", nativeQuery = true)
    List<Object[]> getMonthlyRevenueTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);



    @Query(value = "SELECT CAST(r.payment_date AS DATE) as reportDate, " +
            "COUNT(r.id) as totalCertIssue, " +
            "SUM(r.amount) as totalCollections, " +
            "CASE WHEN MIN(r.or_number) = MAX(r.or_number) THEN MIN(r.or_number) " +
            "ELSE MIN(r.or_number) || ' - ' || MAX(r.or_number) END as orRange " +
            "FROM revenue_record r " +
            "JOIN issued_certificate i ON r.issued_cert_id = i.id " +
            "WHERE i.status = 'RELEASED' " +
            "AND i.is_archive = false " + // 🏛️ Selyadong Filter: Hindi Archived
            "AND r.payment_date BETWEEN :start AND :end " +
            "GROUP BY reportDate " +
            "ORDER BY reportDate DESC", nativeQuery = true)
    List<Object[]> getDailyCollectionsReleasedAndActiveNative(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
