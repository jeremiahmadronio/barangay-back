package com.barangay.barangay.blotter.service;

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

    //  12:00 AM
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void runMediationCheck() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(15);

        List<BlotterCase> neglectedCases = blotterRepository
                .findAllByStatusAndDateFiledBefore(CaseStatus.PENDING, threshold);

        for (BlotterCase bc : neglectedCases) {
            updateCase(bc, CaseStatus.EXPIRED_UNACTIONED, "SYSTEM: Case expired in PENDING status for 15 days.");
        }

        List<BlotterCase> expiredMediation = blotterRepository
                .findAllByStatusAndDateFiledBefore(CaseStatus.UNDER_MEDIATION, threshold);

        for (BlotterCase bc : expiredMediation) {
            updateCase(bc, CaseStatus.REFERRED_TO_LUPON, "SYSTEM: 15-day mediation period ended. Automatically referred to Lupon.");
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
