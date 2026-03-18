package com.barangay.barangay.blotter.dto.complaint;
public record WitnessDTO(
        Long personId,
        String fullName,
        String contactNumber,
        String address,
        String testimony
) {}