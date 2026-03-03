package com.barangay.barangay.users.dto;

public record DashboardStats (
         Long totalUser,
         Long totalActiveUser,
         Long totalCritical,
         Long totalAuditEntry,
         Long auditGrowth,
         String auditDirection

){
}
