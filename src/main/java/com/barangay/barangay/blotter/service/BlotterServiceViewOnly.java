package com.barangay.barangay.blotter.service;

import com.barangay.barangay.blotter.dto.*;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.CaseNote;
import com.barangay.barangay.blotter.model.Hearing;
import com.barangay.barangay.blotter.model.HearingMinutes;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlotterServiceViewOnly {

    private final BlotterCaseRepository blotterRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final WitnessRepository witnessRepository;
    private final HearingRepository  hearingRepository;
    private final HearingMinutesRepository hearingMinutesRepository;
    private final CaseNoteRepository caseNoteRepository;


    @Transactional(readOnly = true)
    public Page<BlotterSummaryDTO> getPagedBlotters(
            String search, String status, Long natureId,
            LocalDate start, LocalDate end, Pageable pageable) {

        String forcedStatus = "RECORDED";

        Specification<BlotterCase> spec = BlotterRecordsSpecificationsFiltering.buildFilter(search, forcedStatus, natureId, start, end);

        return blotterRepository.findAll(spec, pageable).map(bc -> new BlotterSummaryDTO(
                bc.getId(),
                bc.getBlotterNumber(),
                bc.getComplainant().getPerson().getFirstName() + " " + bc.getComplainant().getPerson().getLastName(),
                bc.getRespondent().getPerson().getFirstName() + " " + bc.getRespondent().getPerson().getLastName(),
                bc.getIncidentDetail().getNatureOfComplaint().getName(),
                bc.getDateFiled(),
                bc.getStatus().name()
        ));
    }


    @Transactional(readOnly = true)
    public BlotterRecordViewDTO getFullBlotterRecord(String blotterNumber) {

        BlotterCase bc = blotterRepository.findByBlotterNumber(blotterNumber)
                .orElseThrow(() -> new RuntimeException(" Case not found: " + blotterNumber));

        List<String> evidence = evidenceRecordRepository.findAllByBlotterCase(bc)
                .stream()
                .map(er -> er.getType().getTypeName())
                .toList();

        String officerName = (bc.getReceivingOfficer() != null)
                ? bc.getReceivingOfficer().getFirstName() + " " + bc.getReceivingOfficer().getLastName()
                : "Unassigned / System Generated";

        return new BlotterRecordViewDTO(
                bc.getId(),
                bc.getBlotterNumber(),
                bc.getDateFiled(),
                bc.getStatus().name(),
                officerName,
                bc.getComplainant().getPerson().getFirstName() + " " + bc.getComplainant().getPerson().getLastName(),
                bc.getComplainant().getPerson().getContactNumber(),
                bc.getComplainant().getPerson().getCompleteAddress(),
                bc.getComplainant().getPerson().getCivilStatus(),
                (int) bc.getComplainant().getPerson().getAge(),
                bc.getComplainant().getPerson().getGender(),
                bc.getComplainant().getPerson().getEmail(),
                bc.getRespondent().getPerson().getFirstName() + " " + bc.getRespondent().getPerson().getLastName() ,
                bc.getRespondent().getPerson().getContactNumber(),
                bc.getRespondent().getRelationshipType().getName(),
                bc.getRespondent().getPerson().getCompleteAddress(),
                bc.getIncidentDetail().getNatureOfComplaint().getName(),
                bc.getIncidentDetail().getDateOfIncident(),
                bc.getIncidentDetail().getTimeOfIncident(),
                bc.getIncidentDetail().getPlaceOfIncident(),
                bc.getNarrativeStatement().getStatement(),
                evidence
        );





    }



    @Transactional(readOnly = true)
    public Page<BlotterSummaryDTO> docketTable(
            String search, String status, Long natureId,
            LocalDate start, LocalDate end, Pageable pageable) {

        String statusToExclude = "RECORDED";
        Specification<BlotterCase> spec = BlotterRecordsSpecificationsFiltering.excludeStatus(
                search, statusToExclude, natureId, start, end);

        return blotterRepository.findAll(spec, pageable).map(bc -> new BlotterSummaryDTO(
                bc.getId(),
                bc.getBlotterNumber(),
                bc.getComplainant().getPerson().getFirstName() + " " + bc.getComplainant().getPerson().getLastName(),
                bc.getRespondent().getPerson().getFirstName() + " " + bc.getRespondent().getPerson().getLastName(),
                bc.getIncidentDetail().getNatureOfComplaint().getName(),
                bc.getDateFiled(),
                bc.getStatus().name()
        ));
    }






    @Transactional(readOnly = true)
    public BlotterDocketViewDTO getDocketFullView(String blotterNumber) {
        BlotterCase bc = blotterRepository.findByBlotterNumber(blotterNumber)
                .orElseThrow(() -> new RuntimeException(" Docket Case not found: " + blotterNumber));

        LocalDateTime filedDateTime = bc.getDateFiled();
        LocalDate deadline = (filedDateTime != null) ? filedDateTime.toLocalDate().plusDays(15) : LocalDate.now().plusDays(15);
        long remaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (remaining < 0) remaining = 0;

        List<String> evidence = evidenceRecordRepository.findAllByBlotterCase(bc).stream()
                .map(er -> (er.getType() != null) ? er.getType().getTypeName() : "Unknown Evidence")
                .toList();

        List<WitnessDTO> witnesses = witnessRepository.findAllByBlotterCase(bc).stream()
                .map(w -> new WitnessDTO(
                        (w.getFullName() != null && !w.getFullName().isBlank()) ? w.getFullName() : "Anonymous Witness",
                        (w.getContactNumber() != null && !w.getContactNumber().isBlank()) ? w.getContactNumber() : "N/A",
                        (w.getAddress() != null && !w.getAddress().isBlank()) ? w.getAddress() : "N/A"
                )).toList();


        return new BlotterDocketViewDTO(
                deadline,
                remaining,
                bc.getBlotterNumber(),
                bc.getStatus(),
                bc.getStatusRemarks(),
                bc.getDateFiled(),

                // --- Complainant Info (Safe Navigations) ---
                bc.getComplainant().getPerson().getFirstName(),
                bc.getComplainant().getPerson().getLastName(),
                bc.getComplainant().getPerson().getMiddleName(),
                bc.getComplainant().getPerson().getContactNumber(),
                (bc.getComplainant().getPerson().getAge() != null)
                        ? bc.getComplainant().getPerson().getAge()
                        : (short) 0,
                bc.getComplainant().getPerson().getGender(),
                bc.getComplainant().getPerson().getCivilStatus(),
                bc.getComplainant().getPerson().getEmail(),
                bc.getComplainant().getPerson().getCompleteAddress(),

                // --- Respondent Info (HEAVILY PROTECTED) ---
                bc.getRespondent().getPerson().getFirstName(),
                bc.getRespondent().getPerson().getLastName(),
                bc.getRespondent().getPerson().getMiddleName(),
                bc.getRespondent().getAlias(),
                (bc.getRespondent().getPerson().getContactNumber() != null) ? bc.getRespondent().getPerson().getContactNumber() : "N/A",
                (bc.getRespondent().getPerson().getAge() != null) ? bc.getRespondent().getPerson().getAge().intValue() : 0,
                bc.getRespondent().getPerson().getGender(),
                bc.getRespondent().getDateOfBirth(), // LocalDate can be null
                bc.getRespondent().getPerson().getCivilStatus(),
                bc.getRespondent().getOccupation(),
                (bc.getRespondent().getRelationshipType() != null) ? bc.getRespondent().getRelationshipType().getName() : "Others/Unknown",
                (bc.getRespondent().getPerson().getCompleteAddress() != null) ? bc.getRespondent().getPerson().getCompleteAddress() : "N/A",
                (bc.getRespondent().getLivingWithComplainant() != null) ? bc.getRespondent().getLivingWithComplainant() : false,

                // --- Incident Details (Protected against missing data) ---
                (bc.getIncidentDetail().getNatureOfComplaint() != null) ? bc.getIncidentDetail().getNatureOfComplaint().getName() : "Uncategorized",
                bc.getIncidentDetail().getDateOfIncident(),
                bc.getIncidentDetail().getTimeOfIncident(),
                bc.getIncidentDetail().getPlaceOfIncident(),
                (bc.getIncidentDetail().getFrequency() != null) ? bc.getIncidentDetail().getFrequency().getLabel() : "First Time",
                (bc.getIncidentDetail().getInjuriesDamagesDescription() != null) ? bc.getIncidentDetail().getInjuriesDamagesDescription() : "None reported",

                (bc.getNarrativeStatement() != null) ? bc.getNarrativeStatement().getStatement() : "No narrative statement provided.",

                evidence,
                witnesses
        );
    }




    @Transactional(readOnly = true)
    public MediationProcessDTO getMediationProcess(String blotterNumber) {
        BlotterCase bc = blotterRepository.findByBlotterNumber(blotterNumber)
                .orElseThrow(() -> new RuntimeException("Case not found: " + blotterNumber));

        int hCount = (int) hearingRepository.countByBlotterCaseId(bc.getId());

        boolean s1 = true;
        String s1Date = (bc.getDateFiled() != null)
                ? bc.getDateFiled().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                : "Date not recorded";

        boolean s2 = hCount > 0;
        String s2Status = s2 ? "Summon Issued" : "Awaiting first summon";

        boolean s3 = hCount > 0;

        boolean s4 = List.of(CaseStatus.SETTLED, CaseStatus.DISMISSED, CaseStatus.CERTIFIED_TO_FILE_ACTION)
                .contains(bc.getStatus());

        String s4Status = s4 ? bc.getStatus().name().replace("_", " ") : "Awaiting resolution";

        return new MediationProcessDTO(
                s1,         // stepCaseReceived
                s1Date,     // caseReceivedDate
                s2,         // stepSummonIssued
                s2Status,   // summonStatus
                s3,         // stepMediationOngoing
                hCount,     // hearingsConducted
                s4,         // stepResolved
                s4Status    // resolutionStatus
        );
    }



    @Transactional(readOnly = true)
    public List<HearingViewDTO> getCaseHearings(String blotterNumber) {
        BlotterCase bc = blotterRepository.findByBlotterNumber(blotterNumber)
                .orElseThrow(() -> new RuntimeException("Anlala! Case not found: " + blotterNumber));

        List<Hearing> hearings = hearingRepository.findAllByBlotterCaseIdOrderByScheduledStartAsc(bc.getId());

        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(1);

        return hearings.stream()
                .map(h -> {
                    LocalDate date = h.getScheduledStart().toLocalDate();
                    LocalTime startTime = h.getScheduledStart().toLocalTime();
                    LocalTime endTime = h.getScheduledEnd().toLocalTime();

                    return new HearingViewDTO(
                            h.getId(),
                            h.getSummonNumber().intValue(),
                            h.getStatus().name(),
                            date,
                            startTime,
                            endTime,
                            h.getVenue()
                    );
                }).toList();
    }



    @Transactional(readOnly = true)
    public List<BusySlotDTO> getBusySlots(LocalDate date) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        return hearingRepository.findActiveHearingsByDate(date).stream()
                .map(h -> new BusySlotDTO(
                        h.getScheduledStart().toLocalTime().format(timeFormatter),
                        h.getScheduledEnd().toLocalTime().format(timeFormatter),
                        h.getBlotterCase().getBlotterNumber(),
                        h.getBlotterCase().getIncidentDetail().getNatureOfComplaint().getName()
                )).toList();
    }

    @Transactional(readOnly = true)
    public List<CalendarMarkerDTO> getMonthMarkers(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);

        return hearingRepository.findAllActiveInMonth(start, end).stream()
                .collect(Collectors.groupingBy(h -> h.getScheduledStart().toLocalDate()))
                .entrySet().stream()
                .map(entry -> new CalendarMarkerDTO(entry.getKey().toString(), entry.getValue().size()))
                .toList();
    }




    @Transactional(readOnly = true)
    public MediationHearingViewDTO getHearingView(Long hearingId) {
        Hearing hearing = hearingRepository.findHearingForView(hearingId)
                .orElseThrow(() -> new RuntimeException("Hearing not found."));

        HearingMinutes minutes = hearingMinutesRepository.findByHearingId(hearingId).orElse(null);

        List<HearingParticipantDTO> participants = new ArrayList<>();

        if (hearing.getBlotterCase().getComplainant() != null) {
            String status = (minutes != null && minutes.getComplainantPresent()) ? "Present" : "Absent";
            participants.add(new HearingParticipantDTO(
                    hearing.getBlotterCase().getComplainant().getPerson().getFirstName() + " " + hearing.getBlotterCase().getComplainant().getPerson().getLastName(),
                    "Complainant",
                    status
            ));
        }

        // Respondent Mapping
        if (hearing.getBlotterCase().getRespondent() != null) {
            String status = (minutes != null && minutes.getRespondentPresent()) ? "Present" : "Absent";
            participants.add(new HearingParticipantDTO(
                    hearing.getBlotterCase().getRespondent().getPerson().getFirstName() + " " + hearing.getBlotterCase().getRespondent().getPerson().getLastName(),
                    "Respondent",
                    status
            ));
        }

        return new MediationHearingViewDTO(
                "Hearing " + hearing.getSummonNumber(),
                hearing.getStatus().name(),
                hearing.getScheduledStart().toLocalDate(),
                hearing.getScheduledStart().toLocalTime() + " - " + hearing.getScheduledEnd().toLocalTime(),
                hearing.getVenue(),
                hearing.getBlotterCase().getBlotterNumber(),
                hearing.getBlotterCase().getCaseType().name(),
                "Summon " + hearing.getSummonNumber(),
                participants
        );
    }

    @Transactional(readOnly = true)
    public List<CaseNoteViewDTO> getCaseNotesByNumber(String blotterNumber) {
        if (!blotterRepository.existsByBlotterNumber(blotterNumber)) {
            throw new RuntimeException("Blotter number " + blotterNumber + " not found.");
        }

        List<CaseNote> notes = caseNoteRepository.findByBlotterCaseBlotterNumberOrderByCreatedAtDesc(blotterNumber);

        return notes.stream()
                .map(note -> new CaseNoteViewDTO(
                        note.getId(),
                        note.getNote(),
                        note.getCreatedBy().getFirstName() + " " + note.getCreatedBy().getLastName(),
                        note.getCreatedAt()
                ))
                .toList();
    }



    @Transactional(readOnly = true)
    public DocketStatsDTO getFormalStats() {
        CaseType formal = CaseType.FORMAL_COMPLAINT;

        Set<CaseStatus> activeStatuses = Set.of(
                CaseStatus.PENDING,
                CaseStatus.UNDER_MEDIATION,
                CaseStatus.UNDER_CONCILIATION,
                CaseStatus.REFERRED_TO_LUPON
        );

        Set<CaseStatus> resolvedStatuses = Set.of(
                CaseStatus.SETTLED,
                CaseStatus.CERTIFIED_TO_FILE_ACTION,
                CaseStatus.DISMISSED,
                CaseStatus.ARCHIVED,
                CaseStatus.CLOSED
        );

        return new DocketStatsDTO(
                blotterRepository.countByCaseType(formal),
                blotterRepository.countByCaseTypeAndStatusIn(formal, activeStatuses),
                blotterRepository.countByCaseTypeAndStatusIn(formal, resolvedStatuses),
                blotterRepository.countByCaseTypeAndStatus(formal, CaseStatus.UNDER_MEDIATION)
        );
    }
}