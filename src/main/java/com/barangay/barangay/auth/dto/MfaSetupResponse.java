package com.barangay.barangay.auth.dto;

public record MfaSetupResponse(String secret, String qrCode) {}