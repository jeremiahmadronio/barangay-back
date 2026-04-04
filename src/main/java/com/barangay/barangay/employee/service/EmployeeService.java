package com.barangay.barangay.employee.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.person.dto.EmployeeResponseDTO;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.repository.EmployeeRepository;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeService {


    private final EmployeeRepository employeeRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService  auditLogService;
    private final ObjectMapper objectMapper;



    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getLuponOfficialsPool() {

        final String LUPON_DEPT_NAME = "LUPONG_TAGAPAMAYAPA";
        List<Employee> luponMembers = employeeRepository.findAllByDepartment_NameAndIsActiveTrue(LUPON_DEPT_NAME);

        if (luponMembers.isEmpty()) {
            System.out.println("DEBUG: No active employees found for " + LUPON_DEPT_NAME);
        }

        return luponMembers.stream()
                .map(emp -> new EmployeeResponseDTO(
                        emp.getId(),
                        emp.getPerson().getFirstName() + " " + emp.getPerson().getLastName(),
                        emp.getPosition()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Employee hireEmployee(Long personId, Long deptId, String position, User officer, String ipAddress) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Error: Person not found. Register them first as a Person record."));

        if (employeeRepository.existsByPerson(person)) {
            throw new RuntimeException("Error: This person is already an active employee.");
        }

        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Error: Department not found."));

        Employee employee = new Employee();
        employee.setPerson(person);
        employee.setDepartment(dept);
        employee.setPosition(position);
        employee.setIsActive(true);

        Employee savedEmployee = employeeRepository.save(employee);

        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Employee ID", savedEmployee.getId());
            snapshot.put("Full Name", person.getFirstName() + " " + person.getLastName());
            snapshot.put("Department", dept.getName());
            snapshot.put("Position", savedEmployee.getPosition());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.ADMINISTRATION,
                    "EMPLOYEE_MANAGEMENT",
                    Severity.INFO,
                    "HIRE_EMPLOYEE",
                    ipAddress,
                    "Hired new employee: " + person.getFirstName() + " " + person.getLastName(),
                    null,
                    jsonState
            );
        } catch (Exception e) {
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "AUDIT_LOG_FAIL", ipAddress, e.getMessage(), null, null);
        }

        return savedEmployee;
    }

}
