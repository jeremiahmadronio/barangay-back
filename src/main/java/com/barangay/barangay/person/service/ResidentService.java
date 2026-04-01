package com.barangay.barangay.person.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.person.dto.*;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Resident;
import com.barangay.barangay.person.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final PersonRepository personRepository;
    private final ComplainantRepository complainantRepository;
    private final RespondentRepository respondentRepository;
    private final WitnessRepository  witnessRepository;
    private final AuditLogService  auditLogService;
    private final BlotterCaseRepository blotterCaseRepository;




    @Transactional(readOnly = true)
    public List<PersonSearchResponseDTO> searchPeople(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return personRepository.searchPeopleForBlotter(query.trim());
    }

    @Transactional
    public void registerNewResident(ResidentRegistrationRequestDTO dto , User user , String ipAddress) {
        if (residentRepository.existsByBarangayIdNumber(dto.barangayIdNumber())) {
            throw new RuntimeException("Error: Barangay ID " + dto.barangayIdNumber() + " is already taken!");
        }

        Person person = new Person();
        person.setFirstName(dto.firstName());
        person.setLastName(dto.lastName());
        person.setMiddleName(dto.middleName());
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

        resident.setPerson(person);
        person.setResident(resident);


        personRepository.save(person);


        auditLogService.log(
                user,
                Departments.ADMINISTRATION,
                "RESIDENT_REGISTRATION",
                Severity.INFO,
                "ADD_RESIDENT",
                ipAddress,
                "Register new Resident " + dto.firstName() + " " + dto.lastName(),
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

        // 2. Fetch history using BlotterCaseRepository (Dahil unidirectional ang children)

        // AS COMPLAINANT
        blotterCaseRepository.findAllByComplainant_Person(p).forEach(bc ->
                history.add(mapToHistoryDTO(bc, "COMPLAINANT"))
        );

        // AS RESPONDENT
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
                p.getFirstName(),
                p.getLastName(),
                p.getMiddleName(),
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
        return new ResidentStatsDTO(
                residentRepository.count(),
                residentRepository.countByIsVoterTrue(),
                residentRepository.countSeniorCitizens(),
                residentRepository.countByIsHeadOfFamilyTrue()
        );
    }

}
