package com.barangay.barangay.person.dto;

public record ArchiveStatsDTO(
        Long totalArchived,
        Long totalArchivedResidents,
        Long totalArchivedOfficers,
        Long totalArchivedUsers

) {
}
