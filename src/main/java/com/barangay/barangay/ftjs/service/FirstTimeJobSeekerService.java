package com.barangay.barangay.ftjs.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.FtjsStatus;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.TimeLineType;
import com.barangay.barangay.ftjs.dto.*;
import com.barangay.barangay.ftjs.model.FirstTimeJobSeeker;
import com.barangay.barangay.ftjs.model.FirstTimeJobSeekerAffidavitOfLoss;
import com.barangay.barangay.ftjs.model.FirstTimeJobSeekerNotes;
import com.barangay.barangay.ftjs.model.FirstTimeJobSeekerTimeLine;
import com.barangay.barangay.ftjs.repository.FirstTimeJobSeekerAffidavitOfLossRepository;
import com.barangay.barangay.ftjs.repository.FirstTimeJobSeekerNotesRepository;
import com.barangay.barangay.ftjs.repository.FirstTimeJobSeekerRepository;
import com.barangay.barangay.ftjs.repository.FirstTimeJobSeekerTimeLineRepository;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Resident;
import com.barangay.barangay.person.repository.PersonRepository;
import com.barangay.barangay.person.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirstTimeJobSeekerService {

    private final FirstTimeJobSeekerRepository firstTimeJobSeekerRepository;
    private final FirstTimeJobSeekerAffidavitOfLossRepository firstTimeJobSeekerAffidavitOfLossRepository;
    private final FirstTimeJobSeekerTimeLineRepository firstTimeJobSeekerTimeLineRepository;
    private final FirstTimeJobSeekerNotesRepository firstTimeJobSeekerNotesRepository;
    private final AuditLogService auditLogService;
    private final ResidentRepository residentRepository;
    private final PersonRepository personRepository;


    @Transactional
    public void addRequest(FtjsRequestDTO dto, User actor, String ipAddress) {

        Person person;
        Resident resident = null;
        LocalDate now  =  LocalDate.now();
        if (dto.resident_id() != null) {
            resident = residentRepository.findById(dto.resident_id())
                    .orElseThrow(() -> new RuntimeException("Resident not found: " + dto.resident_id()));
            person = resident.getPerson();
        } else {
            person = new Person();
            person.setFirstName(dto.firstName());
            person.setLastName(dto.lastName());
            person.setGender(dto.gender());
            person.setCompleteAddress(dto.address());
            person.setContactNumber(dto.contactNumber());
            person.setEmail(dto.email());
            person.setIsResident(false);
            person = personRepository.save(person);
        }

        FirstTimeJobSeeker ftjs = new FirstTimeJobSeeker();
        ftjs.setPerson(person);
        ftjs.setFtjsNumber(generateFTJSNumber());
        ftjs.setResident(resident);
        ftjs.setEducationalAttainment(dto.educationalAttainment());
        ftjs.setCourse(dto.course());
        ftjs.setSchoolAddress(dto.schoolAddress());
        ftjs.setSchoolInstitution(dto.institution());
        ftjs.setValidIdType(dto.validIdType());
        ftjs.setIdNumber(dto.idNumber());
        ftjs.setPurposeDocuments(dto.purpose());
        ftjs.setOathFiles(dto.oathFiles());
        ftjs.setStatus(FtjsStatus.ISSUED);
        ftjs.setDateSubmitted(now);
        ftjs.setIssuanceCount(1);
        ftjs.setVerifiedBy(actor);

        FirstTimeJobSeeker saved = firstTimeJobSeekerRepository.save(ftjs);

        saveTimeline(
                saved,
                actor,
                TimeLineType.SUBMITTED,
                "Application Submitted",
                "FTJS application submitted by " + actor.getUsername()
        );

        auditLogService.log(
                actor,
                Departments.FTJS,
                "First Time Job Seeker",
                Severity.INFO,
                "Upload new First time Job Seeker Application",
                ipAddress,
                null,
                null,
                saved
        );
    }

    @Transactional
    public void addNotes (NotesRequestDTO dto ,  User actor, String ipAddress) {

        FirstTimeJobSeeker ftjs = firstTimeJobSeekerRepository.findById(dto.ftjsId()).
                orElseThrow(() -> new RuntimeException("ftjs not found: " + dto.ftjsId()));

        FirstTimeJobSeekerNotes notes =  new FirstTimeJobSeekerNotes();
        notes.setFtjs(ftjs);
        notes.setNotes(dto.notes());
        notes.setCreatedBy(actor);
        firstTimeJobSeekerNotesRepository.save(notes);

        String content = (dto.notes() == null) ? "" : dto.notes();
        String noteSnippet = content.length() > 100
                ? content.substring(0, 97) + "..."
                : content;

        saveTimeline(
                ftjs,
                actor,
                TimeLineType.NOTES_ADDED,
                "Notes Added ",
                noteSnippet
        );
        auditLogService.log(
                actor,
                Departments.FTJS,
                "First Time Job Seeker",
                Severity.INFO,
                "Add notes " + dto.notes(),
                ipAddress,
                noteSnippet,
                null,
                dto
        );
    }


    @Transactional
    public void RequestNewFtjs(RequestNewFtjsDTO dto, User actor, String ipAddress) {

        FirstTimeJobSeeker ftjs = firstTimeJobSeekerRepository.findById(dto.ftjsId())
                .orElseThrow(() -> new RuntimeException("Original FTJS record not found."));

        FirstTimeJobSeekerAffidavitOfLoss affidavit = new FirstTimeJobSeekerAffidavitOfLoss();
        affidavit.setFtjs(ftjs);
        affidavit.setReason(dto.reason());
        affidavit.setAffidavitFiles(dto.affidavitFiles());
        affidavit.setDateOfLoss(dto.dateOfLoss());
        affidavit.setAmountPaid(dto.amountPaid());
        affidavit.setOrNumber(dto.orNumber());
        affidavit.setCreatedBy(actor);

        int nextIssuance = (ftjs.getIssuanceCount() == null ? 1 : ftjs.getIssuanceCount()) + 1;
        affidavit.setIssuanceNumber(nextIssuance);

        ftjs.setIssuanceCount(nextIssuance);
        ftjs.setStatus(FtjsStatus.RE_ISSUANCE);
        firstTimeJobSeekerRepository.save(ftjs);

        FirstTimeJobSeekerAffidavitOfLoss savedAffidavit = firstTimeJobSeekerAffidavitOfLossRepository.save(affidavit);

        saveTimeline(
                ftjs,
                actor,
                TimeLineType.RE_ISSUANCE,
                "Re-issuance Requested",
                "Applicant requested a certificate re-issuance. Reason: " + dto.reason()
        );

        auditLogService.log(
                actor,
                Departments.FTJS,
                "First Time Job Seeker",
                Severity.LOW,
                "Re-issuance requested for " + ftjs.getPerson().getFirstName() + " " + ftjs.getPerson().getLastName(),
                ipAddress,
                dto.reason(),
                null,
                savedAffidavit
        );
    }

    @Transactional(readOnly = true)
    public List<NotesResponseDTO> getNotesByFtjsId(Long ftjsId) {
        List<FirstTimeJobSeekerNotes> notesList = firstTimeJobSeekerNotesRepository.findAllByFtjsIdOrderByCreatedAtDesc(ftjsId);

        return notesList.stream()
                .map(note -> new NotesResponseDTO(
                        note.getId(),
                        note.getNotes(),
                        note.getCreatedBy().getPerson().getFirstName()+ " " + note.getCreatedBy().getPerson().getLastName(),
                        note.getCreatedAt()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<TimelineResponseDTO> getTimeline(Long ftjsId) {

        List<FirstTimeJobSeekerTimeLine> timelineList = firstTimeJobSeekerTimeLineRepository.findAllByFtjsIdOrderByEventDateDesc(ftjsId);
        return timelineList.stream()
                .map(t -> new TimelineResponseDTO(
                        t.getId(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getTimelineType(),
                        t.getEventDate(),
                        t.getCreatedBy() != null ?
                                t.getCreatedBy().getPerson().getFirstName() + " " + t.getCreatedBy().getPerson().getLastName() :
                                "System"
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResponseNewFtjsFullDetailsDTO getAffidavitDetails(Long affidavitId) {

        FirstTimeJobSeekerAffidavitOfLoss affidavit = firstTimeJobSeekerAffidavitOfLossRepository.findById(affidavitId)
                .orElseThrow(() -> new RuntimeException("Affidavit record not found with ID: " + affidavitId));

        return new ResponseNewFtjsFullDetailsDTO(
                affidavit.getId(),
                affidavit.getFtjs().getPerson().getFirstName() + " " + affidavit.getFtjs().getPerson().getLastName(),
                affidavit.getReason(),
                affidavit.getDateOfLoss(),
                affidavit.getIssuanceNumber(),
                affidavit.getAmountPaid(),
                affidavit.getOrNumber(),

                affidavit.getCreatedBy().getPerson().getFirstName() + " " + affidavit.getCreatedBy().getPerson().getLastName(),
                affidavit.getCreatedAt(),
                affidavit.getAffidavitFiles()
        );
    }


    @Transactional(readOnly = true)
    public List<ResponseNewFtjsSummaryDTO> getAffidavitSummary(Long ftjsId) {

    List<FirstTimeJobSeekerAffidavitOfLoss> reports = firstTimeJobSeekerAffidavitOfLossRepository.findAllByFtjsIdOrderByCreatedAtDesc(ftjsId);

    return reports.stream()
            .map(report -> new ResponseNewFtjsSummaryDTO(
                    report.getId(),
                    report.getCreatedAt().toLocalDate(),
                    report.getIssuanceNumber(),
                    report.getReason()
            )).toList();

    }




    @Transactional(readOnly = true)
    public FtjsFullResponseDTO getFullDetails(Long id) {
        FirstTimeJobSeeker ftjs = firstTimeJobSeekerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        return new FtjsFullResponseDTO(
                ftjs.getId(),
                ftjs.getFtjsNumber(),
                ftjs.getStatus(),
                ftjs.getIssuanceCount(),
                ftjs.getResident() != null ? ftjs.getResident().getResidentId() : null,
                ftjs.getPerson().getFirstName() + " " + ftjs.getPerson().getLastName(),
                ftjs.getPerson().getGender(),
                ftjs.getPerson().getContactNumber(),
                ftjs.getPerson().getEmail(),
                ftjs.getPerson().getCompleteAddress(),
                ftjs.getResident() != null,
                ftjs.getSchoolAddress(),
                ftjs.getEducationalAttainment(),
                ftjs.getCourse(),
                ftjs.getSchoolInstitution(),
                ftjs.getValidIdType(),
                ftjs.getIdNumber(),
                ftjs.getPurposeDocuments(),
                ftjs.getDateSubmitted(),
                ftjs.getOathFiles() != null && ftjs.getOathFiles().length > 0,
                ftjs.getOathFiles(),
                ftjs.getVerifiedBy() != null ? ftjs.getVerifiedBy().getUsername() : "N/A",
                ftjs.getCreatedAt(),
                ftjs.getUpdatedAt()
        );
    }




    @Transactional(readOnly = true)
    public List<FtjsTableDTO> getFtjsTableSummary() {
        return firstTimeJobSeekerRepository.findAllSummary();
    }



    @Transactional(readOnly = true)
    public List<ArchiveTableResponseDTO> getArchivedTableSummary() {
        return firstTimeJobSeekerRepository.findAllArchivedSummary();
    }

    @Transactional(readOnly = true)
    public ArchiveResponseDTO getArchiveStats() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        return new ArchiveResponseDTO(
                firstTimeJobSeekerRepository.countByIsArchiveTrue(),
                firstTimeJobSeekerRepository.countArchivedThisMonth(startOfMonth, now),
                firstTimeJobSeekerRepository.countByIsArchiveTrueAndResidentNotNull(),
                firstTimeJobSeekerRepository.countByIsArchiveTrueAndResidentNull()
        );
    }

    @Transactional(readOnly = true)
    public FtjsStatsResponseDTO getFtjsStats() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());



        return new FtjsStatsResponseDTO(
                firstTimeJobSeekerRepository.count(),
                firstTimeJobSeekerRepository.countIssuedThisMonth(firstDayOfMonth, lastDayOfMonth),
                firstTimeJobSeekerRepository.countByIssuanceCount(1),
                firstTimeJobSeekerRepository.countByIssuanceCountGreaterThan(1)
        );


    }




    @Transactional
    public void updateRequest(Long id, FtjsEditRequestDTO dto, User actor, String ipAddress) {
        FirstTimeJobSeeker ftjs = firstTimeJobSeekerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FTJS record not found with ID: " + id));

        Person person = ftjs.getPerson();

        if (dto.firstName() != null) person.setFirstName(dto.firstName());
        if (dto.lastName() != null) person.setLastName(dto.lastName());
        if (dto.gender() != null) person.setGender(dto.gender());
        if (dto.address() != null) person.setCompleteAddress(dto.address());
        if (dto.contactNumber() != null) person.setContactNumber(dto.contactNumber());
        if (dto.email() != null) person.setEmail(dto.email());

        personRepository.save(person);

        if (dto.educationalAttainment() != null) ftjs.setEducationalAttainment(dto.educationalAttainment());
        if (dto.course() != null) ftjs.setCourse(dto.course());
        if (dto.institution() != null) ftjs.setSchoolInstitution(dto.institution());
        if (dto.validIdType() != null) ftjs.setValidIdType(dto.validIdType());
        if (dto.idNumber() != null) ftjs.setIdNumber(dto.idNumber());
        if (dto.purpose() != null) ftjs.setPurposeDocuments(dto.purpose());

        if (dto.oathFiles() != null && dto.oathFiles().length > 0) {
            ftjs.setOathFiles(dto.oathFiles());
        }

        firstTimeJobSeekerRepository.save(ftjs);

        auditLogService.log(
                actor, Departments.FTJS,
                "First Time Job Seeker", Severity.INFO,
                "Updated FTJS record ID: " + id,
                ipAddress, null, null, ftjs
        );
    }


    @Transactional
    public void toggleArchive(Long id, StatusUpdateDTO dto, User actor, String ipAddress) {
        FirstTimeJobSeeker ftjs = firstTimeJobSeekerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FTJS record not found with ID: " + id));

        boolean isArchiving = dto.isArchived();

        ftjs.setArchive(isArchiving);
        ftjs.setArchiveRemarks(isArchiving ? dto.remarks() : null);

        firstTimeJobSeekerRepository.save(ftjs);

        saveTimeline(
                ftjs,
                actor,
                isArchiving ? TimeLineType.ARCHIVED : TimeLineType.RESTORED,
                isArchiving ? "Record was moved to archives" : "Record was restored to active list",
                dto.remarks()
        );


        String logMessage = String.format("%s - FTJS Number: %s | Remarks: %s",
                isArchiving ? "Archived" : "Restored",
                ftjs.getFtjsNumber(),
                dto.remarks());

        auditLogService.log(
                actor,
                Departments.FTJS,
                "First Time Job Seeker",
                Severity.INFO,
                logMessage,
                ipAddress,
                dto.remarks(),
                null,
                ftjs
        );
    }



    private String generateFTJSNumber() {
        return LocalDateTime.now().getYear() + "-FTJS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    //helper for saving timeline
    private void saveTimeline(FirstTimeJobSeeker ftjs, User actor, TimeLineType type, String title, String description) {
        FirstTimeJobSeekerTimeLine timeline = new FirstTimeJobSeekerTimeLine();
        timeline.setFtjs(ftjs);
        timeline.setTimelineType(type);
        timeline.setTitle(title);
        timeline.setDescription(description);
        timeline.setEventDate(LocalDateTime.now());
        timeline.setCreatedBy(actor);
        firstTimeJobSeekerTimeLineRepository.save(timeline);
    }








}
