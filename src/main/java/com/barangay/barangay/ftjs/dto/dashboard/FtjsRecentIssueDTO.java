package com.barangay.barangay.ftjs.dto.dashboard;

import java.time.LocalDateTime;

public record FtjsRecentIssueDTO(
        String ftjsNumber,
        String fullName,
        String status,
        LocalDateTime createdAt
) {
}
