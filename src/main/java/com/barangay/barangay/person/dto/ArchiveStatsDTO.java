package com.barangay.barangay.person.dto;

public record ArchiveStatsDTO(
        Long totalArchived,
        Long totalDeceased,
        Long totalInactive,
        Long totalMoveOut

) {
}
