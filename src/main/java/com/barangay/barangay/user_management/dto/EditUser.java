package com.barangay.barangay.user_management.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record EditUser(
        @Size(min = 2, message = "First name is too short")
        String firstName,

        @Size(min = 2, message = "Last name is too short")
        String lastName,

        @Email(message = "Please provide a valid email address")
        String email,

        @Pattern(regexp = "^(\\+63|0)9\\d{9}$", message = "Invalid PH format")
        String contactNumber,

        Long roleId,

        Set<Long> departmentIds,

        Set<Long> permissionIds,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&.]{8,}$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String password
) {}