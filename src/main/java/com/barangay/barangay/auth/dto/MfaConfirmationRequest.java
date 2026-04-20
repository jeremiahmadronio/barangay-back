package com.barangay.barangay.auth.dto;

public record MfaConfirmationRequest(String secret, String code) {}
