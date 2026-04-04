package com.barangay.barangay.person.dto;

public record UpdateDocumentRequest(
        Long id,
        String documentName,
        String documentType,
        byte[] fileData,
        Boolean isRemoved
) {}