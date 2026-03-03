package com.barangay.barangay.audit.dto;

import java.util.List;

public record AuditFilterOptions(
        List<String> modules,
        List<String> actions,
        List<String> severities
) {}