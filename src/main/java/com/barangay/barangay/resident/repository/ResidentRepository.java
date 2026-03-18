package com.barangay.barangay.resident.repository;

import com.barangay.barangay.resident.dto.ResidentSummary;
import com.barangay.barangay.resident.model.People;
import com.barangay.barangay.resident.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    boolean existsByBarangayIdNumber(String barangayIdNumber);


    @Query("SELECT new com.barangay.barangay.resident.dto.ResidentSummary(" +
            "r.residentId, r.barangayIdNumber, " +
            "CONCAT(r.person.firstName, ' ', r.person.lastName), " +
            "r.person.contactNumber, r.householdNumber , r.isVoter) " +
            "FROM Resident r")
    List<ResidentSummary> getMinimalResidentList();






}

