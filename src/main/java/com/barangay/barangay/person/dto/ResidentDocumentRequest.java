package com.barangay.barangay.person.dto;

public record ResidentDocumentRequest (
        String documentName,
        String documentType,
        byte[] fileData
){
}
