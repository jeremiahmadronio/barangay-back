package com.barangay.barangay.vawc.dto;

import java.time.LocalDateTime;

public record DisplayCFADTO (
        String blotterNumber,
        String matterFiled,
        String complainantName,
        String complainantAddress,
        String respondentName,
        String respondentAddress,
        String grounds,
        String controlNumber,
        LocalDateTime issuedAt,
        String assignOfficerName,
        String assignOfficerPosition
) {
}
