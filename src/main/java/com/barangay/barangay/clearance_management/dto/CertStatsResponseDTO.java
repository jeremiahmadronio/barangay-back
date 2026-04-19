package com.barangay.barangay.clearance_management.dto;

public record CertStatsResponseDTO (
        long totalCertificate,
        long totalPaidCertificate,
        long totalFreeCertificate,
        long totalTemplate

) {
}
