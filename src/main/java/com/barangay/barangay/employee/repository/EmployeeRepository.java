package com.barangay.barangay.employee.repository;

import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.person.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByPersonId(Long personId);

    List<Employee> findAllByDepartment_NameAndIsActiveTrue(String deptName);

    boolean existsByPerson(Person person);

}
