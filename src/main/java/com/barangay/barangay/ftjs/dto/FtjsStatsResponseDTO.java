package com.barangay.barangay.ftjs.dto;

public record FtjsStatsResponseDTO (
        long totalCertificatesIssued,
        long totalCertificatedThisMonth,
        long originalIssuances,
        long reIssuances

) {
}
