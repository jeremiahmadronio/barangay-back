package com.barangay.barangay.clearance_management.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

public record TemplateRequestDTO (
        String certTitle,
        String layoutStyle,
        String certTagline,
        JsonNode bodySections,
        JsonNode issueFields,
        boolean requiresPhoto,
        boolean requiresThumbmark,
        boolean hasFee,
        boolean hasCtn,
        BigDecimal certFee,
        Integer validityMonths,
        String footerText,
        java.util.List<SignatoryDTO> signatories
) {
}
