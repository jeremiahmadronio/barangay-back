    package com.barangay.barangay.blotter.dto.reports_and_display;

    import com.barangay.barangay.enumerated.CaseType;

    import java.time.LocalDateTime;
    public record ArchiveTableDTO(
            Long caseId,
            String blotterNumber,
            String caseType,
            String complainant,
            String respondent,
            String status,
            String archivedRemarks,
            LocalDateTime dateFiled
    ) {}