package com.barangay.barangay.ftjs.dto;

public record FtjsEditRequestDTO(
        String firstName,
        String lastName,
        String gender,
        String address,
        String contactNumber,
        String email,
        String educationalAttainment,
        String course,
        String institution,
        String validIdType,
        String idNumber,
        byte[] oathFiles,
        String purpose
) {
}
