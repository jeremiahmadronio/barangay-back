package com.barangay.barangay.person.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.employee.repository.EmployeeRepository;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.person.dto.*;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Resident;
import com.barangay.barangay.person.model.ResidentDocument;
import com.barangay.barangay.person.repository.*;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final PersonRepository personRepository;
    private final UserManagementRepository userManagementRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService  auditLogService;
    private final BlotterCaseRepository blotterCaseRepository;




    @Transactional(readOnly = true)
    public List<PersonSearchResponseDTO> searchPeople(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        String trimmedQuery = query.trim();
        return personRepository.searchPeopleForBlotter(trimmedQuery);
    }

    @Transactional
    public void registerNewResident(ResidentRegistrationRequestDTO dto , User user , String ipAddress) {
        if (residentRepository.existsByBarangayIdNumber(dto.barangayIdNumber())) {
            throw new RuntimeException("Error: Barangay ID " + dto.barangayIdNumber() + " is already taken!");
        }


        boolean personExists = personRepository.existsByFirstNameAndLastNameAndMiddleNameAndBirthDateAndSuffixAndGender(
                dto.firstName(),
                dto.lastName(),
                dto.middleName(),
                dto.birthDate(),
                dto.suffix(),
                dto.gender()
        );

        if (personExists) {
            throw new RuntimeException("Error: Person is Already Registered!");
        }

        Person person = new Person();
        person.setFirstName(dto.firstName());
        person.setLastName(dto.lastName());
        person.setMiddleName(dto.middleName());
        person.setSuffix(dto.suffix());

        person.setContactNumber(dto.contactNumber());
        person.setCompleteAddress(dto.completeAddress());

        person.setAge(dto.age());
        person.setBirthDate(dto.birthDate());
        person.setGender(dto.gender());

        person.setCivilStatus(dto.civilStatus());
        person.setEmail(dto.email());
        person.setPhoto(dto.photo());

        person.setOccupation(dto.occupation());

        person.setIsResident(true);

        Resident resident = new Resident();
        resident.setHouseholdNumber(dto.householdNumber());
        resident.setPrecinctNumber(dto.precinctNumber());
        resident.setIsVoter(dto.isVoter());
        resident.setIsHeadOfFamily(dto.isHeadOfFamily());

        resident.setCitizenship(dto.citizenship());
        resident.setReligion(dto.religion());
        resident.setBloodType(dto.bloodType());

        resident.setBarangayIdNumber(dto.barangayIdNumber());
        resident.setDateOfResidency(dto.dateOfResidency());

        resident.setIs4ps(dto.is4ps());
        resident.setIsPwd(dto.isPwd());
        resident.setPwdIdNumber(dto.pwdIdNumber());
        resident.setIsIndigent(dto.isIndigent());
        resident.setEducationalAttainment(dto.educationalAttainment());

        if (dto.documents() != null && !dto.documents().isEmpty()) {
            List<ResidentDocument> docEntities = dto.documents().stream().map(docDto -> {
                ResidentDocument doc = new ResidentDocument();
                doc.setDocumentName(docDto.documentName());
                doc.setDocumentType(docDto.documentType());
                doc.setFileData(docDto.fileData());
                doc.setStatus(ResidentDocumentStatus.ACTIVE); // Default to ACTIVE
                doc.setResident(resident); // Link back to resident
                return doc;
            }).collect(Collectors.toList());

            resident.setDocuments(docEntities);
        }

        resident.setPerson(person);
        person.setResident(resident);


        personRepository.save(person);


        auditLogService.log(
                user,
                Departments.ADMINISTRATION,
                "Resident Management",
                Severity.INFO,
                "Register new Resident — " + dto.firstName() + " " + dto.lastName(),
                ipAddress,
                null,
                null,
                dto

        );


    }



    @Transactional(readOnly = true)
    public ResidentProfileViewDTO getFullResidentProfile(Long residentId) {
        // 1. Fetch Resident and its associated Person
        Resident res = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found with ID: " + residentId));
        Person p = res.getPerson();

        List<ResidentCaseHistoryDTO> history = new ArrayList<>();


        blotterCaseRepository.findAllByComplainant_Person(p).forEach(bc ->
                history.add(mapToHistoryDTO(bc, "COMPLAINANT"))
        );

        blotterCaseRepository.findAllByRespondent_Person(p).forEach(bc ->
                history.add(mapToHistoryDTO(bc, "RESPONDENT"))
        );

        // AS WITNESS
        blotterCaseRepository.findAllByWitnessPerson(p).forEach(bc ->
                history.add(mapToHistoryDTO(bc, "WITNESS"))
        );

        // 3. Sort history by dateFiled (latest first)
        history.sort(Comparator.comparing(ResidentCaseHistoryDTO::dateFiled,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // 4. Construct the completed DTO
        return new ResidentProfileViewDTO(
                p.getId(),
                p.getPhoto(),
                p.getFirstName(),
                p.getLastName(),
                p.getMiddleName(),
                p.getSuffix(),
                p.getFirstName() + " " + (p.getMiddleName() != null ? p.getMiddleName() + " " : "") + p.getLastName(),
                p.getGender(),
                p.getBirthDate(),
                p.getAge(),
                p.getCivilStatus(),
                p.getContactNumber(),
                p.getEmail(),
                p.getCompleteAddress(),
                p.getOccupation(),
                res.getBarangayIdNumber(),
                res.getHouseholdNumber(),
                res.getPrecinctNumber(),
                res.getCitizenship(),
                res.getReligion(),
                res.getBloodType(),
                res.getIsVoter(),
                res.getIsHeadOfFamily(),
                res.getDateOfResidency(),
                res.getIs4ps(),
                res.getIsPwd(),
                res.getPwdIdNumber(),
                res.getIsIndigent(),
                res.getEducationalAttainment(),
                res.getStatus(),

                history
        );

    }

    private ResidentCaseHistoryDTO mapToHistoryDTO(BlotterCase bc, String role) {
        String nature = "General Record";
        if (bc.getIncidentDetail() != null && bc.getIncidentDetail().getNatureOfComplaint() != null) {
            nature = bc.getIncidentDetail().getNatureOfComplaint();
        }

        return new ResidentCaseHistoryDTO(
                bc.getBlotterNumber(),
                nature,
                role,
                bc.getStatus() != null ? bc.getStatus().name() : "PENDING",
                bc.getDateFiled()
        );
    }


    @Transactional(readOnly = true)
    public List<ResidentSummary> getResidentTable(String search, String gender, Boolean isVoter, String household) {
        String genderFilter = (gender == null || gender.equalsIgnoreCase("All") || gender.isEmpty()) ? null : gender;

        return residentRepository.findWithFilters(search, genderFilter, isVoter, household);
    }



    @Transactional(readOnly = true)
    public ResidentStatsDTO getResidentDashboardStats() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                .truncatedTo(ChronoUnit.DAYS);

        ResidentStatus active = ResidentStatus.ACTIVE;

        return new ResidentStatsDTO(
                residentRepository.countByCreatedDateAfterAndStatus(startOfMonth, active),
                residentRepository.countByIsVoterTrueAndStatus(active),
                residentRepository.countActiveSeniorCitizens(),
                residentRepository.countByIsHeadOfFamilyTrueAndStatus(active)
        );
    }


    @Transactional
    public void updateResidentProfile(Long residentId, UpdateResidentProfileDTO dto, User actor, String ipAddress) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found with ID: " + residentId));
        Person person = resident.getPerson();

        Map<String, Object> oldState = captureResidentState(resident, person);

        updatePersonDetails(person, dto);

        updateResidentDetails(resident, dto);

        if (dto.documents() != null) {
            handleDocumentProcessing(resident, dto.documents());
        }

        personRepository.save(person);

        auditLogService.log(
                actor,
                Departments.ADMINISTRATION,
                "Resident Management",
                Severity.INFO,
                "Resident Profile updated — " + resident.getPerson().getFirstName() + " " + resident.getPerson().getLastName(),
                ipAddress,
                null,
                oldState,
                captureResidentState(resident, person)
        );
    }

    private void updatePersonDetails(Person p, UpdateResidentProfileDTO dto) {
        if (dto.firstName() != null) p.setFirstName(dto.firstName());
        if (dto.lastName() != null) p.setLastName(dto.lastName());
        if (dto.middleName() != null) p.setMiddleName(dto.middleName());
        if (dto.suffix() != null) p.setSuffix(dto.suffix());
        if (dto.contactNumber() != null) p.setContactNumber(dto.contactNumber());
        if (dto.completeAddress() != null) p.setCompleteAddress(dto.completeAddress());
        if (dto.age() != null) p.setAge(dto.age());
        if (dto.birthDate() != null) p.setBirthDate(dto.birthDate());
        if (dto.gender() != null) p.setGender(dto.gender());
        if (dto.civilStatus() != null) p.setCivilStatus(dto.civilStatus());
        if (dto.email() != null) p.setEmail(dto.email());
        if (dto.occupation() != null) p.setOccupation(dto.occupation());
        if (dto.photo() != null) p.setPhoto(dto.photo().length == 0 ? null : dto.photo());
    }

    private void updateResidentDetails(Resident r, UpdateResidentProfileDTO dto) {
        if (dto.householdNumber() != null) r.setHouseholdNumber(dto.householdNumber());
        if (dto.precinctNumber() != null) r.setPrecinctNumber(dto.precinctNumber());
        if (dto.isVoter() != null) r.setIsVoter(dto.isVoter());
        if (dto.isHeadOfFamily() != null) r.setIsHeadOfFamily(dto.isHeadOfFamily());
        if (dto.citizenship() != null) r.setCitizenship(dto.citizenship());
        if (dto.religion() != null) r.setReligion(dto.religion());
        if (dto.bloodType() != null) r.setBloodType(dto.bloodType());
        if (dto.is4ps() != null) r.setIs4ps(dto.is4ps());
        if (dto.isPwd() != null) r.setIsPwd(dto.isPwd());
        if (dto.pwdIdNumber() != null) r.setPwdIdNumber(dto.pwdIdNumber());
        if (dto.isIndigent() != null) r.setIsIndigent(dto.isIndigent());
        if (dto.educationalAttainment() != null) r.setEducationalAttainment(dto.educationalAttainment());
        if (dto.dateOfResidency() != null) r.setDateOfResidency(dto.dateOfResidency());

        if (dto.barangayIdNumber() != null && !dto.barangayIdNumber().equals(r.getBarangayIdNumber())) {
            if (residentRepository.existsByBarangayIdNumber(dto.barangayIdNumber())) {
                throw new RuntimeException("Barangay ID " + dto.barangayIdNumber() + " is already taken!");
            }
            r.setBarangayIdNumber(dto.barangayIdNumber());
        }
    }


    private void handleDocumentProcessing(Resident resident, List<UpdateDocumentRequest> docs) {
        for (UpdateDocumentRequest docDto : docs) {
            if (docDto.id() == null) {

                ResidentDocument newDoc = new ResidentDocument();
                newDoc.setDocumentName(docDto.documentName());
                newDoc.setDocumentType(docDto.documentType());
                newDoc.setFileData(docDto.fileData());
                newDoc.setStatus(ResidentDocumentStatus.ACTIVE);
                newDoc.setResident(resident);
                resident.getDocuments().add(newDoc);
            } else {
                resident.getDocuments().stream()
                        .filter(d -> d.getId().equals(docDto.id()))
                        .findFirst()
                        .ifPresent(existingDoc -> {
                            if (Boolean.TRUE.equals(docDto.isRemoved())) {
                                existingDoc.setStatus(ResidentDocumentStatus.DELETED);
                            } else {
                                if (docDto.documentName() != null) existingDoc.setDocumentName(docDto.documentName());
                                if (docDto.fileData() != null && docDto.fileData().length > 0) {
                                    existingDoc.setFileData(docDto.fileData());
                                }
                            }
                        });
            }
        }
    }

    private Map<String, Object> captureResidentState(Resident r, Person p) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("FullName", p.getFirstName() + " " + p.getLastName());
        state.put("BarangayID", r.getBarangayIdNumber());
        state.put("Household", r.getHouseholdNumber());
        state.put("Voter", r.getIsVoter());
        state.put("PWD", r.getIsPwd());
        return state;
    }


    @Transactional(readOnly = true)
    public ResidentSuggestionsDTO getResidentSuggestions() {
        Random random = new Random();
        String currentYear = String.valueOf(LocalDate.now().getYear());

        String hhPrefix = currentYear + "-HH-";
        String suggestedHousehold = residentRepository.findLastHouseholdByPrefix(hhPrefix)
                .map(lastH -> {
                    try {
                        int lastSeq = Integer.parseInt(lastH.substring(lastH.lastIndexOf("-") + 1));
                        return String.format("%s%04d", hhPrefix, lastSeq + 1);
                    } catch (Exception e) {
                        return hhPrefix + "0001";
                    }
                })
                .orElse(hhPrefix + "0001");

        String bidPrefix =  currentYear + "-BID"  + "-";
        String nextBarangayId = residentRepository.findLastBarangayIdByPrefix(bidPrefix)
                .map(lastId -> {
                    try {
                        int lastSequence = Integer.parseInt(lastId.substring(lastId.lastIndexOf("-") + 1));
                        return String.format("%s%04d", bidPrefix, lastSequence + 1);
                    } catch (Exception e) {
                        return bidPrefix + "0001";
                    }
                })
                .orElse(bidPrefix + "0001");


        int pNum = random.nextInt(10000);
        char pLetter = (char) ('A' + random.nextInt(4));
        String suggestedPrecinct = String.format("%04d-%c", pNum, pLetter);

        return new ResidentSuggestionsDTO(nextBarangayId, suggestedPrecinct, suggestedHousehold);
    }




    @Transactional(readOnly = true)
    public ResidentFullProfileViewDTO getFullResidentProfileAdmin(Long residentId) {
        Resident res = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found with ID: " + residentId));
        Person p = res.getPerson();

        // 1. Fetch Active Documents only
        List<ResidentDocumentViewDTO> documents = res.getDocuments().stream()
                .filter(doc -> doc.getStatus() == ResidentDocumentStatus.ACTIVE) // Soft Delete Filter
                .map(doc -> new ResidentDocumentViewDTO(
                        doc.getId(),
                        doc.getDocumentName(),
                        doc.getDocumentType(),
                        doc.getFileData(),
                        doc.getUploadedAt()
                ))
                .collect(Collectors.toList());

        List<ResidentCaseHistoryDTO> history = new ArrayList<>();
        blotterCaseRepository.findAllByComplainant_Person(p).forEach(bc -> history.add(mapToHistoryDTO(bc, "COMPLAINANT")));
        blotterCaseRepository.findAllByRespondent_Person(p).forEach(bc -> history.add(mapToHistoryDTO(bc, "RESPONDENT")));
        blotterCaseRepository.findAllByWitnessPerson(p).forEach(bc -> history.add(mapToHistoryDTO(bc, "WITNESS")));

        history.sort(Comparator.comparing(ResidentCaseHistoryDTO::dateFiled, Comparator.nullsLast(Comparator.reverseOrder())));

        return new ResidentFullProfileViewDTO(
                p.getId(),
                p.getPhoto(),
                p.getFirstName(),
                p.getLastName(),
                p.getMiddleName(),
                p.getSuffix(),
                p.getFirstName() + " " + (p.getMiddleName() != null ? p.getMiddleName() + " " : "") + p.getLastName(),
                p.getGender(),
                p.getBirthDate(),
                p.getAge(),
                p.getCivilStatus(),
                p.getContactNumber(),
                p.getEmail(),
                p.getCompleteAddress(),
                p.getOccupation(),
                res.getBarangayIdNumber(),
                res.getHouseholdNumber(),
                res.getPrecinctNumber(),
                res.getCitizenship(),
                res.getReligion(),
                res.getBloodType(),
                res.getIsVoter(),
                res.getIsHeadOfFamily(),
                res.getDateOfResidency(),
                res.getIs4ps(),
                res.getIsPwd(),
                res.getPwdIdNumber(),
                res.getIsIndigent(),
                res.getEducationalAttainment(),
                res.getStatus(),
                history,
                documents
        );
    }



    @Transactional
    public void updateResidentStatus(Long residentId, UpdateStatusDTO dto, User actor, String ipAddress) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found with ID: " + residentId));

        ResidentStatus oldStatus = resident.getStatus();

        if (oldStatus == dto.status()) {
            throw new RuntimeException("Resident is already in " + dto.status() + " status!");
        }

        resident.setStatus(dto.status());
        resident.setStatusRemarks(dto.reason());
        residentRepository.save(resident);

        auditLogService.log(
                actor,
                Departments.ADMINISTRATION,
                "Resident Management",
                Severity.WARNING,
                "Update Status for — " + resident.getPerson().getFirstName() + " " + resident.getPerson().getLastName(),
                ipAddress,
                dto.reason(),
                oldStatus.name(),
                dto.status().name()
        );
    }




    @Transactional(readOnly = true)
    public ArchiveStatsDTO getArchiveStats() {
        long archivedResidents = residentRepository.countByStatus(ResidentStatus.ARCHIVED);
        long archivedOfficers = employeeRepository.countByStatus(Status.ARCHIVED);
        long archivedUsers = userManagementRepository.countByStatus(Status.ARCHIVED);

        long total = archivedResidents + archivedOfficers + archivedUsers;

        return new ArchiveStatsDTO(
                total,
                archivedResidents,
                archivedOfficers,
                archivedUsers
        );
    }



}
