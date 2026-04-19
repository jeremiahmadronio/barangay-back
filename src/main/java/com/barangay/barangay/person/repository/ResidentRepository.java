package com.barangay.barangay.person.repository;

import com.barangay.barangay.enumerated.ResidentStatus;
import com.barangay.barangay.person.dto.ResidentSummary;
import com.barangay.barangay.person.model.Resident;
import com.barangay.barangay.user_management.dto.AdminDashboaradResidentByStatusDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    boolean existsByBarangayIdNumber(String barangayIdNumber);

    long countByStatus(ResidentStatus status);


    @Query("SELECT new com.barangay.barangay.user_management.dto.AdminDashboaradResidentByStatusDTO(CAST(r.status AS string), COUNT(r)) " +
            "FROM Resident r GROUP BY r.status")
    List<AdminDashboaradResidentByStatusDTO> getResidentStatusCounts();

    Long countByCreatedDateAfter(LocalDateTime date);
    List<Resident> findTop5ByOrderByCreatedDateDesc();


    @Query("SELECT new com.barangay.barangay.person.dto.ResidentSummary(" +
            "r.residentId, r.barangayIdNumber, " +
            "r.person.photo, " +
            "CONCAT(r.person.firstName, ' ', r.person.lastName), " +
            "r.person.contactNumber, r.householdNumber, r.isVoter, r.status, r.statusRemarks) " +
            "FROM Resident r " +
            "WHERE (:search IS NULL OR CAST(:search AS string) = '' " +
            "OR LOWER(CONCAT(r.person.firstName, ' ', r.person.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(r.barangayIdNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:gender IS NULL OR CAST(:gender AS string) = '' OR r.person.gender = CAST(:gender AS string)) " +
            "AND (:isVoter IS NULL OR r.isVoter = :isVoter) " +
            "AND (:household IS NULL OR CAST(:household AS string) = '' OR r.householdNumber LIKE CONCAT('%', CAST(:household AS string), '%'))")
    List<ResidentSummary> findWithFilters(
            @Param("search") String search,
            @Param("gender") String gender,
            @Param("isVoter") Boolean isVoter,
            @Param("household") String household

    );



    long countByIsVoterTrue();

    long countByIsHeadOfFamilyTrue();


    @Query("SELECT COUNT(r) FROM Resident r WHERE r.person.age IS NOT NULL AND r.person.age >= 60")
    long countSeniorCitizens();


    @Query("SELECT r.barangayIdNumber FROM Resident r WHERE r.barangayIdNumber LIKE :prefix% ORDER BY r.barangayIdNumber DESC LIMIT 1")
    Optional<String> findLastBarangayIdByPrefix(@Param("prefix") String prefix);

    @Query("SELECT r.householdNumber FROM Resident r WHERE r.householdNumber LIKE :prefix% ORDER BY r.householdNumber DESC LIMIT 1")
    Optional<String> findLastHouseholdByPrefix(@Param("prefix") String prefix);





    long countByCreatedDateAfterAndStatus(LocalDateTime date, ResidentStatus status);

    // Selyado: Bilangin lang ang ACTIVE voters
    long countByIsVoterTrueAndStatus(ResidentStatus status);

    // Selyado: Bilangin lang ang ACTIVE heads of family
    long countByIsHeadOfFamilyTrueAndStatus(ResidentStatus status);

    // Selyado: Custom Query para sa ACTIVE Senior Citizens
    @Query("""
        SELECT COUNT(r) FROM Resident r 
        WHERE r.person.age IS NOT NULL 
        AND r.person.age >= 60 
        AND r.status = com.barangay.barangay.enumerated.ResidentStatus.ACTIVE
    """)
    long countActiveSeniorCitizens();

}

