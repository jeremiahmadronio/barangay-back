package com.barangay.barangay.user_management.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateUser (

        @NotNull(message = "Person ID is required")
        Long personId,

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 20)
        String username,

        @NotBlank(message = "System email is required")
        @Email(message = "Please provide a valid email address")
        @Pattern(
                regexp = "^[A-Za-z0-9+_.-]+@(.+)$",
                message = "Email format is invalid"
        )
        String systemEmail,

        @NotNull(message = "Role ID is required")
        Long roleId,

        @NotEmpty(message = "Select at least one department")
        Set<Long> departmentIds,

        Set<Long> permissionIds,

        boolean activateImmediately

) {


}
