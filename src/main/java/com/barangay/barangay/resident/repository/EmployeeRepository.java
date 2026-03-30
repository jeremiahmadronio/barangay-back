package com.barangay.barangay.resident.repository;

import com.barangay.barangay.resident.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByPersonId(Long personId);

    List<Employee> findAllByDepartment_NameAndIsActiveTrue(String deptName);
}
