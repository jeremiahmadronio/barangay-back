package com.barangay.barangay.clearance_management.repository;

import com.barangay.barangay.clearance_management.dto.ArchiveSummaryResponseDTO;
import com.barangay.barangay.clearance_management.dto.RecentRequestResponseDTO;
import com.barangay.barangay.clearance_management.model.IssuedCertificate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IssuedCertificateRepository extends JpaRepository<IssuedCertificate, Long> {

    long countByIsFreeFalse(); // Total Paid
    long countByIsFreeTrue();


    @Query("SELECT COUNT(i) FROM IssuedCertificate i " +
            "WHERE i.status = 'RELEASED' AND i.isArchive = false " +
            "AND i.issuedAt BETWEEN :start AND :end")
    long countIssuedToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(r.amount) FROM RevenueRecord r " +
            "JOIN r.issuedCertificate i " +
            "WHERE i.status = 'RELEASED' AND i.isArchive = false " +
            "AND r.paymentDate BETWEEN :start AND :end")
    BigDecimal sumRevenueToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM IssuedCertificate i " +
            "WHERE i.isArchive = true " +
            "AND i.issuedAt BETWEEN :start AND :end")
    long countArchiveToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM IssuedCertificate i " +
            "WHERE i.status = 'RELEASED' AND i.isArchive = false " +
            "AND i.isFree = true " +
            "AND i.issuedAt BETWEEN :start AND :end")
    long countFreeCertsToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT CAST(i.issued_at AS DATE) as issueDate, COUNT(i.id) as total " +
            "FROM issued_certificate i " +
            "WHERE i.status = 'RELEASED' AND i.is_archive = false " +
            "AND i.issued_at BETWEEN :start AND :end " +
            "GROUP BY issueDate " +
            "ORDER BY issueDate ASC", nativeQuery = true)
    List<Object[]> getWeeklyIssuedTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query("SELECT new com.barangay.barangay.clearance_management.dto.RecentRequestResponseDTO(" +
            "CONCAT(p.firstName, ' ', p.lastName), " +
            "t.certTitle, " +
            "i.issuedAt, " +
            "CAST(i.status AS string)) " +
            "FROM IssuedCertificate i " +
            "JOIN i.person p " +
            "JOIN i.template t " +
            "WHERE i.status = com.barangay.barangay.enumerated.ClearanceStatus.RELEASED " +
            "AND i.isArchive = false " +
            "ORDER BY i.issuedAt DESC")
    List<RecentRequestResponseDTO> findRecentRequests(Pageable pageable);

    @Query("SELECT new com.barangay.barangay.clearance_management.dto.ArchiveSummaryResponseDTO(" +
            "i.id, i.certNumber, CONCAT(p.firstName, ' ', p.lastName), " +
            "t.certTitle, t.certFee, CAST(i.status AS string), i.archiveRemarks) " +
            "FROM IssuedCertificate i " +
            "JOIN i.person p " +
            "JOIN i.template t " +
            "WHERE i.isArchive = true " +
            "ORDER BY i.issuedAt DESC")
    List<ArchiveSummaryResponseDTO> findAllArchived();

    @Query("SELECT COUNT(i) FROM IssuedCertificate i WHERE i.isArchive = true")
    long countTotalArchiveIssued();

    @Query("SELECT SUM(r.amount) FROM RevenueRecord r " +
            "JOIN r.issuedCertificate i WHERE i.isArchive = true")
    BigDecimal sumLostRevenue();

    @Query(value = "SELECT t.cert_title FROM issued_certificate i " +
            "JOIN certificate_template t ON i.template_id = t.id " +
            "WHERE i.is_archive = true " +
            "GROUP BY t.cert_title ORDER BY COUNT(i.id) DESC LIMIT 1",
            nativeQuery = true)
    String findMostArchivedTemplateName();
}
