package com.barangay.barangay.scheduler;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.CaseNote;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.blotter.repository.CaseNoteRepository;
import com.barangay.barangay.enumerated.CaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseBackgroundService15DaysChecker {

    private final BlotterCaseRepository blotterRepository;
    private final CaseNoteRepository caseNoteRepository;

    private static final String LUPON_DEPT_NAME = "LUPONG_TAGAPAMAYAPA";

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void runDailyMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime neglectThreshold = now.minusDays(15);
        List<BlotterCase> neglectedBlotters = blotterRepository
                .findAllByStatusAndDepartmentIsNullAndDateFiledBefore(
                        CaseStatus.PENDING, neglectThreshold);

        for (BlotterCase bc : neglectedBlotters) {
            updateCase(bc, CaseStatus.EXPIRED_UNACTIONED,
                    "SYSTEM: Entry expired. No department assigned within the 15-day window.");
        }


        List<BlotterCase> expiredLuponCases = blotterRepository
                .findAllByStatusAndDepartmentNameAndLuponReferral_DeadlineBefore    (
                        CaseStatus.UNDER_CONCILIATION, LUPON_DEPT_NAME, now);

        for (BlotterCase bc : expiredLuponCases) {
            updateCase(bc, CaseStatus.CLOSED,
                    "SYSTEM: Mediation period ended. Deadline reached without settlement.");
        }
    }

    private void updateCase(BlotterCase bc, CaseStatus targetStatus, String reason) {
        bc.setStatus(targetStatus);
        blotterRepository.save(bc);

        CaseNote systemNote = new CaseNote();
        systemNote.setBlotterCase(bc);
        systemNote.setNote(reason);
        caseNoteRepository.save(systemNote);
    }
}
