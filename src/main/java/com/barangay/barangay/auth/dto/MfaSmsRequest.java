package com.barangay.barangay.auth.dto;

public record MfaSmsRequest(
        String phoneNumber,
        String code
) {}