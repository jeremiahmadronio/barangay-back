package com.barangay.barangay.ftjs.dto.dashboard;

import com.barangay.barangay.enumerated.FtjsStatus;

public record StatusCountDTO(
        FtjsStatus status,
        long total
) {
}