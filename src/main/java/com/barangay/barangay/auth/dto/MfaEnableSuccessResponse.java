package com.barangay.barangay.auth.dto;

import java.util.Set;

public record MfaEnableSuccessResponse(String status, Set<String> recoveryCodes) {}