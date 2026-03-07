package com.barangay.barangay.admin_management.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record CreateAdmin (

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
        String username,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address (must contain @)")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&.]{8,}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
        )
        String password,

        @NotBlank(message = "Contact number is required")
        @Pattern(
                regexp = "^(\\+63|0)9\\d{9}$",
                message = "Invalid PH format. Use +639xxxxxxxxx or 09xxxxxxxxx"
        )
        String contactNumber,

        @NotNull(message = "Role ID is required")
        Long roleId,

        boolean allDepartments,

        Set<Long> departmentIds,

        boolean activateImmediately
) {
}