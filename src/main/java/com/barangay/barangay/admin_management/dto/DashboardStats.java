package com.barangay.barangay.admin_management.dto;

public record DashboardStats (
        Long totalUser,
        Long totalActiveResident,
        Long totalActiveEmployee,
        Long totalAuditEntry,
        Long auditGrowth,
        String auditDirection

){
}
