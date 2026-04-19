package com.barangay.barangay.clearance_management.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record IssuanceRequestDTO(
        Long templateId,
        Long personId,
        String requestorName,
        JsonNode fieldValues,
        String orNumber,
        String ctnNumber,
        boolean isFree,
        String remarks
) {}