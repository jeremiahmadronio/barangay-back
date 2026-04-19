package com.barangay.barangay.scheduler;

import com.barangay.barangay.blotter.model.Hearing;
import com.barangay.barangay.blotter.repository.HearingRepository;
import com.barangay.barangay.enumerated.HearingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HearingStatusScheduler {

    private final HearingRepository hearingRepository;

    //every 5 minutes
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoUpdateHearingStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<Hearing> overdueHearings = hearingRepository.findOverdueHearings(now);

        if (!overdueHearings.isEmpty()) {
            log.info("Brutal System Update: Found {} overdue hearings. Marking as PENDING_MINUTES.", overdueHearings.size());

            for (Hearing h : overdueHearings) {
                h.setStatus(HearingStatus.PENDING_MINUTES);
            }
            hearingRepository.saveAll(overdueHearings);
        }
    }
}