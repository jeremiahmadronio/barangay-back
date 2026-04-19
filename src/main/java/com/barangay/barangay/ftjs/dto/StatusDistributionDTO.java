package com.barangay.barangay.ftjs.dto;

import com.barangay.barangay.enumerated.FtjsStatus;

public record StatusDistributionDTO  (
        FtjsStatus status,
        long total
){
}
