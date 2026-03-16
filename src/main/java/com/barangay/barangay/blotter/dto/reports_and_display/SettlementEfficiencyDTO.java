package com.barangay.barangay.blotter.dto.reports_and_display;

public record SettlementEfficiencyDTO(
        long totalFormalComplaints,
        long settledCases,
        double efficiencyPercentage
) {}