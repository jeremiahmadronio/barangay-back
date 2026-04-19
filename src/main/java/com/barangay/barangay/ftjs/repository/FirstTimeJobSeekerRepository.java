package com.barangay.barangay.ftjs.repository;

import com.barangay.barangay.enumerated.FtjsStatus;
import com.barangay.barangay.ftjs.dto.ArchiveTableResponseDTO;
import com.barangay.barangay.ftjs.dto.FtjsReportTableDTO;
import com.barangay.barangay.ftjs.dto.FtjsTableDTO;
import com.barangay.barangay.ftjs.dto.StatusDistributionDTO;
import com.barangay.barangay.ftjs.dto.dashboard.FtjsRecentIssueDTO;
import com.barangay.barangay.ftjs.dto.dashboard.StatusCountDTO;
import com.barangay.barangay.ftjs.model.FirstTimeJobSeeker;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FirstTimeJobSeekerRepository  extends JpaRepository<FirstTimeJobSeeker,Long> {

    @Query("SELECT new com.barangay.barangay.ftjs.dto.FtjsTableDTO(" +
            "f.id, " +
            "CAST(f.ftjsNumber AS string), " +
            "CONCAT(f.person.firstName, ' ', f.person.lastName), " +
            "f.issuanceCount, " +
            "CAST(f.status AS string), " +
            "f.dateSubmitted, " +
            "CASE WHEN f.resident IS NOT NULL THEN true ELSE false END) " +
            "FROM FirstTimeJobSeeker f " +
            "ORDER BY f.dateSubmitted DESC")
    List<FtjsTableDTO> findAllSummary();

    @Query("SELECT COUNT(f) FROM FirstTimeJobSeeker f " +
            "WHERE f.dateSubmitted BETWEEN :start AND :end")
    long countIssuedThisMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    long countByIssuanceCount(Integer count);

    long countByIssuanceCountGreaterThan(Integer count);

    long countByStatus(FtjsStatus status);



    @Query("SELECT new com.barangay.barangay.ftjs.dto.ArchiveTableResponseDTO(" +
            "f.id, " +
            "CAST(f.ftjsNumber AS string), " +
            "CONCAT(f.person.firstName, ' ', f.person.lastName), " +
            "f.issuanceCount, " +
            "CAST(f.status AS string), " +
            "f.dateSubmitted, " +
            "f.archiveRemarks) " +
            "FROM FirstTimeJobSeeker f " +
            "WHERE f.isArchive = true " +
            "ORDER BY f.updatedAt DESC")
    List<ArchiveTableResponseDTO> findAllArchivedSummary();

    long countByIsArchiveFalse();

    long countByStatusInAndDateSubmittedBetween(
            List<FtjsStatus> statuses,
            LocalDate start,
            LocalDate end);

        long countByStatusInAndResidentNotNullAndDateSubmittedBetween(
            List<FtjsStatus> statuses,
            LocalDate start,
            LocalDate end);

        long countByStatusInAndResidentNullAndDateSubmittedBetween(
            List<FtjsStatus> statuses,
            LocalDate start,
            LocalDate end);



    @Query("SELECT new com.barangay.barangay.ftjs.dto.StatusDistributionDTO(f.status, COUNT(f)) " +
            "FROM FirstTimeJobSeeker f " +
            "WHERE f.dateSubmitted BETWEEN :start AND :end " +
            "GROUP BY f.status")
    List<StatusDistributionDTO> getStatusDistribution(@Param("start") LocalDate start, @Param("end") LocalDate end);




    @Query(value = "SELECT TO_CHAR(date_submitted, 'Mon DD') as label, COUNT(*) as total " +
            "FROM first_time_job_seeker " +
            "WHERE status IN ('ISSUED', 'RE_ISSUANCE') " +
            "AND date_submitted BETWEEN :start AND :end " +
            "GROUP BY date_submitted " +
            "ORDER BY date_submitted ASC", nativeQuery = true)
    List<Object[]> getDailyTrend(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(value = "SELECT TO_CHAR(date_submitted, 'Month YYYY') as label, COUNT(*) as total " +
            "FROM first_time_job_seeker " +
            "WHERE status IN ('ISSUED', 'RE_ISSUANCE') " +
            "AND date_submitted BETWEEN :start AND :end " +
            "GROUP BY DATE_TRUNC('month', date_submitted), label " +
            "ORDER BY DATE_TRUNC('month', date_submitted) ASC", nativeQuery = true)
    List<Object[]> getMonthlyTrend(@Param("start") LocalDate start, @Param("end") LocalDate end);


    @Query("SELECT new com.barangay.barangay.ftjs.dto.FtjsReportTableDTO(" +
            "f.id, " +
            "CAST(f.ftjsNumber AS string), " +
            "CONCAT(f.person.firstName, ' ', f.person.lastName), " +
            "CAST(f.status AS string), " +
            "f.dateSubmitted, " +
            "f.person.contactNumber) " +
            "FROM FirstTimeJobSeeker f " +
            "WHERE f.status IN (com.barangay.barangay.enumerated.FtjsStatus.ISSUED, " +
            "                 com.barangay.barangay.enumerated.FtjsStatus.RE_ISSUANCE) " +
            "AND f.dateSubmitted BETWEEN :start AND :end " +
            "ORDER BY f.dateSubmitted DESC")
    List<FtjsReportTableDTO> findReportCasesInRange(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);




    @Query("SELECT COUNT(f) FROM FirstTimeJobSeeker f " +
            "WHERE f.status IN (com.barangay.barangay.enumerated.FtjsStatus.ISSUED, " +
            "                 com.barangay.barangay.enumerated.FtjsStatus.RE_ISSUANCE) " +
            "AND f.dateSubmitted = :today")
    long countIssuedToday(@Param("today") LocalDate today);

    @Query("SELECT COUNT(f) FROM FirstTimeJobSeeker f " +
            "WHERE f.status IN (com.barangay.barangay.enumerated.FtjsStatus.ISSUED, " +
            "                 com.barangay.barangay.enumerated.FtjsStatus.RE_ISSUANCE) " +
            "AND f.dateSubmitted BETWEEN :start AND :end")
    long countIssuedInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(f) FROM FirstTimeJobSeeker f " +
            "WHERE f.isArchive = true " +
            "AND f.updatedAt BETWEEN :start AND :end")
    long countArchivedInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(f) FROM FirstTimeJobSeeker f " +
            "WHERE f.status IN (com.barangay.barangay.enumerated.FtjsStatus.ISSUED, " +
            "                 com.barangay.barangay.enumerated.FtjsStatus.RE_ISSUANCE) " +
            "AND f.resident IS NULL " +
            "AND f.dateSubmitted BETWEEN :start AND :end")
    long countNonResidentIssuedInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);




    @Query("SELECT new com.barangay.barangay.ftjs.dto.dashboard.StatusCountDTO(f.status, COUNT(f)) " +
            "FROM FirstTimeJobSeeker f " +
            "GROUP BY f.status")
    List<StatusCountDTO> findAllStatusCounts();


    @Query("SELECT new com.barangay.barangay.ftjs.dto.dashboard.FtjsRecentIssueDTO(" +
            "CAST(f.ftjsNumber AS string), " +
            "CONCAT(f.person.firstName, ' ', f.person.lastName), " +
            "CAST(f.status AS string), " +
            "f.createdAt) " +
            "FROM FirstTimeJobSeeker f " +
            "WHERE f.status IN (com.barangay.barangay.enumerated.FtjsStatus.ISSUED, " +
            "                 com.barangay.barangay.enumerated.FtjsStatus.RE_ISSUANCE) " +
            "ORDER BY f.createdAt DESC " +
            "LIMIT 5")
    List<FtjsRecentIssueDTO> findTop5RecentIssues();



    long countByIsArchiveTrue();

    // 🏛️ Total Archived This Month (Base sa updatedAt)
    @Query("SELECT COUNT(f) FROM FirstTimeJobSeeker f " +
            "WHERE f.isArchive = true " +
            "AND f.updatedAt BETWEEN :start AND :end")
    long countArchivedThisMonth(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 🏛️ Archived Residents
    long countByIsArchiveTrueAndResidentNotNull();

    // 🏛️ Archived Non-Residents (Walk-ins)
    long countByIsArchiveTrueAndResidentNull();

}
