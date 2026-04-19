package com.barangay.barangay.employee.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.department.repository.UserDepartmentRepository;
import com.barangay.barangay.employee.dto.*;
import com.barangay.barangay.employee.repository.EmployeeCaseRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.person.dto.EmployeeResponseDTO;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.repository.EmployeeRepository;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeService {


    private final EmployeeRepository employeeRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService  auditLogService;
    private final ObjectMapper objectMapper;
    private final UserDepartmentRepository userDepartmentRepository;
   private final EmployeeCaseRepository caseRepository;


    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getLuponOfficialsPool() {

        final String LUPON_DEPT_NAME = "LUPONG_TAGAPAMAYAPA";
        List<Employee> luponMembers = employeeRepository.findAllByDepartment_NameAndStatus(LUPON_DEPT_NAME, Status.ACTIVE);

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
    public void hireEmployee(AddEmployeeDTO request, User officer, String ipAddress) {
        Person person = personRepository.findById(request.personId())
                .orElseThrow(() -> new RuntimeException("Resource Not Found: Person ID " + request.personId() + " not found."));

        if (employeeRepository.existsByPerson(person)) {
            throw new RuntimeException("Conflict: Person " + person.getFirstName() + " is already an active employee.");
        }

        Department dept;
        if (request.isGlobal()) {
            dept = departmentRepository.findByName("ADMINISTRATION")
                    .orElseThrow(() -> new RuntimeException("Resource Not Found: Administration department not found in database."));
        } else {
            if (request.departmentId() == null) {
                throw new RuntimeException("Bad Request: Department ID is required for non-global employees.");
            }
            dept = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("Resource Not Found: Department ID " + request.departmentId() + " not found."));
        }

        Employee employee = new Employee();
        employee.setPerson(person);
        employee.setDepartment(dept);
        employee.setPosition(request.position());
        employee.setStatus(request.status());

        Employee savedEmployee = employeeRepository.save(employee);

            logHireAudit(savedEmployee, person, dept, officer, ipAddress);


    }

    private void logHireAudit(Employee emp, Person p, Department d, User officer, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Officer_ID", emp.getId());
            snapshot.put("Full_Name", p.getFirstName() + " " + p.getLastName());
            snapshot.put("Department", d.getName());
            snapshot.put("Position", emp.getPosition());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.ADMINISTRATION,
                    "Officer Management",
                    Severity.INFO,
                    "Register new Officer — " + p.getFirstName() + " " + p.getLastName(),
                    ip,
                    null,
                    null,
                    jsonState
            );
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public EmployeeStatsDTO getScopedEmployeeStats(UUID userId) {
        User officer = userDepartmentRepository.findByIdWithDepartments(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Set<Department> depts = officer.getAllowedDepartments();
        List<Long> deptIds = depts.stream().map(Department::getId).toList();


        if (deptIds.isEmpty()) return new EmployeeStatsDTO(0, 0, 0, 0);

        long total = employeeRepository.countByDepartmentIdIn(deptIds);
        long active = employeeRepository.countByDepartmentIdInAndStatus(deptIds, Status.ACTIVE);
        long inactive = employeeRepository.countByDepartmentIdInAndStatus(deptIds, Status.INACTIVE);

        long deptCount = depts.size();

        return new EmployeeStatsDTO(total, active, inactive, deptCount);
    }



    @Transactional(readOnly = true)
    public EmployeeStatsDTO getGlobalEmployeeStats() {

        long total = employeeRepository.countAllGlobal();
        long active = employeeRepository.countByStatusGlobal(Status.ACTIVE);
        long inactive = employeeRepository.countByStatusGlobal(Status.INACTIVE);

        long deptCount = employeeRepository.countAllDepartments();

        return new EmployeeStatsDTO(total, active, inactive, deptCount);
    }



    @Transactional(readOnly = true)
    public Page<EmployeeTableDTO> getPaginatedEmployees(
            UUID adminId,
            String search,
            Long filterDeptId,
            Status status,
            Pageable pageable
    ) {
        User admin = userDepartmentRepository.findByIdWithDepartments(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<Long> allowedDeptIds = admin.getAllowedDepartments().stream()
                .map(Department::getId)
                .toList();

        if (allowedDeptIds.isEmpty()) return Page.empty();

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return employeeRepository.findEmployeeTableWithFilters(
                allowedDeptIds,
                searchParam,
                filterDeptId,
                status,
                pageable
        );
    }



    @Transactional(readOnly = true)
    public Page<EmployeeTableDTO> getGlobalPaginatedEmployees(
            String search,
            Long filterDeptId,
            Status status,
            Pageable pageable
    ) {
        List<String> excludedDepts = List.of("ADMIN", "ROOT_ADMIN");

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        return employeeRepository.findGlobalEmployeeTableWithFilters(
                excludedDepts,
                searchParam,
                filterDeptId,
                status,
                pageable
        );
    }


    @Transactional(readOnly = true)
    public EmployeeViewDTO getEmployeeFullDetails(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException(" Employee not found!"));

        var person = emp.getPerson();
        var dept = emp.getDepartment();

        List<EmployeeAssignCaseDTO> assignedCases = caseRepository.findCasesByEmployeeId(employeeId);

        return new EmployeeViewDTO(
                emp.getId(),
                person.getFirstName() + " " + person.getLastName(),
                person.getPhoto(),
                emp.getStatus(),
                emp.getStatusRemarks(),
                person.getEmail(),
                person.getContactNumber(),
                person.getBirthDate(),
                person.getAge(),
                person.getGender(),
                person.getCivilStatus(),
                person.getCompleteAddress(),
                dept.getName(),
                emp.getPosition(),
                assignedCases
        );
    }


    @Transactional
    public void updateEmployeeStatus(Long id, UpdateEmployeeStatus request, User officer, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: Employee with ID " + id + " does not exist."));

        if (employee.getStatus().equals(request.newStatus())) {
            throw new RuntimeException("Conflict: Employee status is already " + request.newStatus());
        }

        String oldStatus = employee.getStatus().toString();
        String newStatus = request.newStatus().toString();

        employee.setStatus(request.newStatus());
        employee.setStatusRemarks(request.reason());
        Employee savedEmployee = employeeRepository.save(employee);

        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("employeeId", savedEmployee.getId());
            snapshot.put("fullName", savedEmployee.getPerson().getFirstName() + " " + savedEmployee.getPerson().getLastName());
            snapshot.put("statusTransition", oldStatus + " -> " + newStatus);
            snapshot.put("reason", request.reason());

            auditLogService.log(
                    officer,
                    Departments.ADMINISTRATION,
                    "Officer Management",
                    Severity.INFO,
                    "Update Status for — " + employee.getPerson().getFirstName() + " " + employee.getPerson().getLastName(),
                    ipAddress,
                    request.reason(),
                    oldStatus,
                    newStatus

            );
        } catch (Exception e) {
            System.err.println("Audit Log Failure: " + e.getMessage());
        }
    }


    @Transactional
    public void editEmployee(Long id, EditEmployeeDTO request, User officer, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: Employee with ID " + id + " not found."));

        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();

        if (request.departmentId() != null && !request.departmentId().equals(employee.getDepartment().getId())) {
            Department newDept = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("Resource Not Found: Department ID " + request.departmentId() + " not found."));

            oldValues.put("department", employee.getDepartment().getName());
            employee.setDepartment(newDept);
            newValues.put("department", newDept.getName());
        }

        if (request.position() != null && !request.position().trim().isEmpty() && !request.position().equals(employee.getPosition())) {
            oldValues.put("position", employee.getPosition());
            employee.setPosition(request.position());
            newValues.put("position", employee.getPosition());
        }

        if (request.status() != null && !request.status().equals(employee.getStatus())) {
            oldValues.put("status", employee.getStatus().toString());
            employee.setStatus(request.status());
            newValues.put("status", employee.getStatus().toString());
        }

        if (oldValues.isEmpty()) {
            return ;
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        logEditAudit(updatedEmployee, oldValues, newValues, officer, ipAddress);

    }

    private void logEditAudit(Employee emp, Map<String, Object> oldV, Map<String, Object> newV, User officer, String ip) {
        try {
            String jsonOld = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(oldV);
            String jsonNew = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(newV);

            auditLogService.log(
                    officer,
                    Departments.ADMINISTRATION,
                    "Officer Management",
                    Severity.INFO,
                    "Officer Profile Update — " + emp.getPerson().getFirstName() + " " + emp.getPerson().getLastName() ,
                    ip,
                    "Updated details for employee: " + emp.getPerson().getFirstName() + " " + emp.getPerson().getLastName(),
                    jsonOld,
                    jsonNew
            );
        } catch (Exception e) {
            System.err.println("Audit Log Failure: " + e.getMessage());
        }
    }
}
