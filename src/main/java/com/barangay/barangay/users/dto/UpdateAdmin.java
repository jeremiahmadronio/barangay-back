package com.barangay.barangay.users.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record UpdateAdmin(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Contact number is required")
        @Pattern(regexp = "^(\\+63|0)9\\d{9}$", message = "Invalid PH format")
        String contactNumber,

        @NotNull(message = "Role ID is required")
        Long roleId,

        boolean allDepartments,
        Set<Long> departmentIds

) {
}