package com.barangay.barangay.clearance_management.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

public record TemplateResponseDTO(
        Long id,
        String certTitle,
        String layoutStyle,
        String certTagline,
        JsonNode bodySections,
      JsonNode issueFields,
        boolean hasFee,
        BigDecimal certFee,
        boolean hascTn,
        Integer validityMonths,
        java.util.List<SignatoryDTO> signatories
) {}