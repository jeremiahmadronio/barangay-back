package com.barangay.barangay.admin_management.dto;

public record DashboardStats (
         Long totalUser,
         Long totalActiveUser,
         Long totalCritical,
         Long totalAuditEntry,
         Long auditGrowth,
         String auditDirection

){
}
