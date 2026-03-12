package com.barangay.barangay.enumerated;

public enum CaseStatus {
    // 1. Initial State
    PENDING,

    // 2. Active States
    UNDER_MEDIATION,    // Kasalukuyang nasa 15-day PB mediation
    UNDER_CONCILIATION, // Elevated na sa Lupon/Pangkat

    // 3. Automated/Timeout State
    EXPIRED_UNACTIONED, // Na-stuck sa Pending/Mediation nang walang galaw (15-day limit)

    // 4. Successful End States
    SETTLED,            // May areglong nangyari (Success!)
    RECORDED,           // Para sa "For the Record" cases lang

    // 5. Unsuccessful End States
    REFERRED_TO_LUPON,  // Failed sa PB level, pinapasa na sa Lupon
    CERTIFIED_TO_FILE_ACTION,
    DISMISSED,          // Binawi, kulang sa ebidensya, o hindi sumipot ang complainant

    // 6. Maintenance States
    ARCHIVED,           // Tapos na at matagal na
    CLOSED              // General closure
}