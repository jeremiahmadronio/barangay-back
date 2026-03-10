package com.barangay.barangay.admin_management.dto;

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


        @Pattern(
                regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be empty or contain at least one uppercase, one lowercase, one number, and one special character (min 8 chars)"
        )
        String password,

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Contact number is required")
        @Pattern(regexp = "^(\\+63|0)9\\d{9}$", message = "Invalid PH format")
        String contactNumber,


        boolean allDepartments,
        Set<Long> departmentIds

) {
}