package com.barangay.barangay.employee.repository;

import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.vawc.dto.AssignOfficerOptionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByPersonId(Long personId);

    List<Employee> findAllByDepartment_NameAndIsActiveTrue(String deptName);

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
        WHERE e.isActive = true
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
        WHERE e.isActive = true
        AND (UPPER(d.name) = 'VAWC')
    """)
    List<AssignOfficerOptionDTO> findAssignOfficerOptioNComplaint();

}
