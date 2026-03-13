package com.barangay.barangay.blotter.dto.reports;

public record SettlementEfficiencyDTO(
        long totalFormalComplaints,
        long settledCases,
        double efficiencyPercentage
) {}