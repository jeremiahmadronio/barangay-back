package com.barangay.barangay.person.dto;

import java.time.LocalDateTime;

public record ResidentDocumentViewDTO(
        Long id,
        String documentName,
        String documentType,
        byte[] fileData,
        LocalDateTime uploadedAt
) {
}
