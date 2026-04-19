package com.barangay.barangay.vawc.dto.projection;

public interface ReportStatsProjection {

    Long getTotalCases();
    Long getTotalExpired();
    Long getResolvedCases();
    Double getAvgResolutionTime();
}
