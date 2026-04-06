package com.barangay.barangay.employee.repository;

import com.barangay.barangay.employee.dto.EmployeeTableDTO;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.vawc.dto.AssignOfficerOptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    long countByDepartmentIdIn(List<Long> deptIds);
    long countByDepartmentIdInAndStatus(List<Long> deptIds, Status status);

    Optional<Employee> findByPersonId(Long personId);

    List<Employee> findAllByDepartment_NameAndStatus(String deptName, Status status);

    boolean existsByPerson(Person person);

    @Query("""
        SELECT new com.barangay.barangay.vawc.dto.AssignOfficerOptionDTO(
            e.id, 
            CONCAT(p.firstName, ' ', p.lastName), 
            e.position
        )
        FROM Employee e
        JOIN e.person p
        JOIN e.department d
        WHERE e.status = com.barangay.barangay.enumerated.Status.ACTIVE
        AND (UPPER(d.name) = 'ADMINISTRATION' OR UPPER(d.name) = 'VAWC')
    """)
    List<AssignOfficerOptionDTO> findAssignOfficerOptionDTO();

    @Query("""
        SELECT new com.barangay.barangay.vawc.dto.AssignOfficerOptionDTO(
            e.id, 
            CONCAT(p.firstName, ' ', p.lastName), 
            e.position
        )
        FROM Employee e
        JOIN e.person p
        JOIN e.department d
        WHERE e.status = com.barangay.barangay.enumerated.Status.ACTIVE
        AND (UPPER(d.name) = 'VAWC')
    """)
    List<AssignOfficerOptionDTO> findAssignOfficerOptioNComplaint();



    @Query("""
    SELECT new com.barangay.barangay.employee.dto.EmployeeTableDTO(
        e.id,
        CONCAT(p.firstName, ' ', p.lastName),
       p.email,
        d.name,
        e.position,
        CAST(e.status AS string),
        (SELECT COUNT(c) FROM BlotterCase c 
         WHERE c.employee = e 
         AND c.status IN (com.barangay.barangay.enumerated.CaseStatus.PENDING, 
                          com.barangay.barangay.enumerated.CaseStatus.UNDER_MEDIATION, 
                          com.barangay.barangay.enumerated.CaseStatus.UNDER_CONCILIATION, 
                          com.barangay.barangay.enumerated.CaseStatus.ELEVATED_TO_FORMAL))
    )
    FROM Employee e
    JOIN e.person p
    JOIN e.department d
    LEFT JOIN User u ON u.person = p
    WHERE d.id IN :allowedDeptIds
    AND (:search IS NULL OR 
         UPPER(CAST(CONCAT(p.firstName, ' ', p.lastName) AS string)) LIKE UPPER(CONCAT('%', CAST(:search AS string), '%')) OR 
         UPPER(CAST(e.position AS string)) LIKE UPPER(CONCAT('%', CAST(:search AS string), '%')) OR 
         UPPER(CAST(u.systemEmail AS string)) LIKE UPPER(CONCAT('%', CAST(:search AS string), '%')))
    AND (:filterDeptId IS NULL OR d.id = :filterDeptId)
    AND (:status IS NULL OR e.status = :status)
""")
    Page<EmployeeTableDTO> findEmployeeTableWithFilters(
            @Param("allowedDeptIds") List<Long> allowedDeptIds,
            @Param("search") String search,
            @Param("filterDeptId") Long filterDeptId,
            @Param("status") Status status,
            Pageable pageable
    );

}
