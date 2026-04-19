package com.barangay.barangay.vawc.service;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.vawc.model.BaranggayProtectionOrder;
import com.barangay.barangay.vawc.repository.BarangayProtectionOrderRepository;
import com.barangay.barangay.vawc.repository.VawcCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VawcBackgroundService {

    private final VawcCaseRepository caseRepository;
    private final BarangayProtectionOrderRepository  barangayProtectionOrderRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processVawcAutomatedStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Starting VAWC Background Maintenance Task at {}", now);

        LocalDateTime oneDayAgo = now.minusDays(1);
        List<BlotterCase> unattendedCases = caseRepository.findPendingVawcToExpire(
                "VAWC", CaseStatus.PENDING, oneDayAgo);

        for (BlotterCase bc : unattendedCases) {
            bc.setStatus(CaseStatus.EXPIRED_UNACTIONED);
            bc.setStatusRemarks("SYSTEM AUTO-REPLY: No action taken within 24 hours of filing.");
            caseRepository.save(bc);
            log.warn("Case #{} marked as UNACTIONED due to inactivity.", bc.getBlotterNumber());
        }

        LocalDateTime fifteenDaysAgo = now.minusDays(15);
        List<BaranggayProtectionOrder> expiredBpos = barangayProtectionOrderRepository.findExpiredActiveBpos(
                "VAWC", CaseStatus.UNDER_MEDIATION, fifteenDaysAgo);

        for (BaranggayProtectionOrder bpo : expiredBpos) {
            BlotterCase bc = bpo.getBlotterCase();

            if (bc.getStatus() == CaseStatus.UNDER_MEDIATION) {
                bc.setStatus(CaseStatus.CLOSED);
                bc.setStatusRemarks("SYSTEM AUTO-CLOSE: 15-day BPO period ended without resolution or escalation.");
                bc.setSettledAt(now);
                caseRepository.save(bc);
                log.info("Case #{} auto-closed after 15 days of mediation.", bc.getBlotterNumber());
            }
        }
    }
}
