package com.barangay.barangay.clearance_management.repository;

import com.barangay.barangay.clearance_management.dto.RevenueResponseByCertificate;
import com.barangay.barangay.clearance_management.dto.TopTemplateResponseDTO;
import com.barangay.barangay.clearance_management.model.CertificateTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {
    boolean existsByCertTitle(String certTitle);

    List<CertificateTemplate> findAllByHasArchiveFalse();

    long countByHasArchiveFalse();


    @Query("SELECT new com.barangay.barangay.clearance_management.dto.RevenueResponseByCertificate(" +
            "t.certTitle, " +
            "COUNT(i.id), " +
            "t.certFee, " +
            "SUM(r.amount)) " +
            "FROM CertificateTemplate t " +
            "LEFT JOIN t.issuedCertificates i ON i.status = 'RELEASED' " +
            "AND i.isArchive = false " +
            "AND i.issuedAt BETWEEN :startDate AND :endDate " +
            "LEFT JOIN i.revenueRecord r " +
            "GROUP BY t.certTitle, t.certFee " +
            "ORDER BY t.certFee DESC")
    List<RevenueResponseByCertificate> getRevenueByCertificateType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);



    @Query("SELECT new com.barangay.barangay.clearance_management.dto.RevenueResponseByCertificate(" +
            "t.certTitle, " +
            "COUNT(i.id), " +
            "t.certFee, " +
            "SUM(r.amount)) " +
            "FROM CertificateTemplate t " +
            "JOIN t.issuedCertificates i ON i.status = 'RELEASED' " +
            "JOIN i.revenueRecord r " +
            "WHERE i.isArchive = false " +
            "AND i.issuedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY t.certTitle, t.certFee " +
            "ORDER BY SUM(r.amount) DESC")
    List<RevenueResponseByCertificate> findTopRevenueCertificates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COUNT(t) FROM CertificateTemplate t WHERE t.hasArchive = true")
    long countTotalArchiveTemplate();

    @Query("SELECT new com.barangay.barangay.clearance_management.dto.TopTemplateResponseDTO(" +
            "t.certTitle, COUNT(i.id)) " +
            "FROM CertificateTemplate t " +
            "JOIN t.issuedCertificates i " +
            "WHERE i.status = 'RELEASED' " +
            "AND i.isArchive = false " +
            "AND t.hasArchive = false " +
            "GROUP BY t.certTitle " +
            "ORDER BY COUNT(i.id) DESC")
    List<TopTemplateResponseDTO> findTopTemplates(Pageable pageable);


    @Query(value = "SELECT cert_title FROM certificate_template " +
            "WHERE has_archive = true " +
            "ORDER BY updated_at DESC LIMIT 1",
            nativeQuery = true)
    String findLatestArchivedTemplateName();

}
