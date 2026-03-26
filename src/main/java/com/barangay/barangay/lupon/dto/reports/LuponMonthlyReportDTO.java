package com.barangay.barangay.lupon.dto.reports;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LuponMonthlyReportDTO {
    private LocalDate dateFiled;
    private String caseNo;
    private String parties;
    private String complaint;

    // Nature
    private Integer isCriminal = 0;
    private Integer isCivil = 0;
    private Integer isOthers = 0;

    // Settled
    private Integer mediation = 0;
    private Integer conciliation = 0;
    private Integer arbitration = 0;

    // Unsettled/Status
    private Integer ongoing = 0;
    private Integer dismissed = 0;
    private Integer issueCFA = 0;
    private Integer withdrawn = 0;

}
