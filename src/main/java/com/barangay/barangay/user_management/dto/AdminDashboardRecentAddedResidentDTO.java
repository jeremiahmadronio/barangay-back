package com.barangay.barangay.user_management.dto;

public record AdminDashboardRecentAddedResidentDTO(
        String barangayIdNumber,
        String fullName,
        Boolean isVoter,
        String status
) {
}
