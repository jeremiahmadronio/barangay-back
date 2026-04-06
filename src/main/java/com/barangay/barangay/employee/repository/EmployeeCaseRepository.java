package com.barangay.barangay.employee.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.employee.dto.EmployeeAssignCaseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeCaseRepository extends JpaRepository<BlotterCase, Long> {

    @Query("""
    SELECT new com.barangay.barangay.employee.dto.EmployeeAssignCaseDTO(
        c.id,
        c.blotterNumber,
        i.natureOfComplaint,
        CAST(c.status AS string),
        c.dateFiled,
        CONCAT(comp.person.firstName, ' ', comp.person.lastName)
    )
    FROM BlotterCase c
    JOIN c.incidentDetail i
    JOIN c.complainant comp
    WHERE c.employee.id = :employeeId
    ORDER BY c.dateFiled DESC
""")
    List<EmployeeAssignCaseDTO> findCasesByEmployeeId(@Param("employeeId") Long employeeId);
}
