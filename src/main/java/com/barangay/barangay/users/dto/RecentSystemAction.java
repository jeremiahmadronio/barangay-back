package com.barangay.barangay.users.dto;

import java.time.LocalDateTime;

public record RecentSystemAction (
        String firstName,
        String lastName,
        String severity,
        String actionTaken,
        String module,
        LocalDateTime createdAt
){
}
