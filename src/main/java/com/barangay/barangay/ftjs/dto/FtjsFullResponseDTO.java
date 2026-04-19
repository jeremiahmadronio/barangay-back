package com.barangay.barangay.ftjs.dto;

import com.barangay.barangay.enumerated.FtjsStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FtjsFullResponseDTO(
        Long id,
        String trackingNumber,
        FtjsStatus status,
        Integer issuanceCount,

        Long residentId,
        String fullName,
        String gender,
        String contactNumber,
        String email,
        String fullAddress,
        boolean isRegisteredResident,


        String schoolAddress,
        String educationalAttainment,
        String course,
        String institution,
        String validIdType,
        String idNumber,
        String purpose,
        LocalDate dateSubmitted,

        boolean hasOathFile,
        byte[] oathFile,

        String verifiedBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}