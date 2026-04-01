package com.barangay.barangay.employee.service;

import com.barangay.barangay.person.dto.EmployeeResponseDTO;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeService {


    private final EmployeeRepository employeeRepository;



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

}
