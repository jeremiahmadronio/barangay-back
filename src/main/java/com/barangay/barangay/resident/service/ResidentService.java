package com.barangay.barangay.resident.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.resident.dto.*;
import com.barangay.barangay.resident.model.People;
import com.barangay.barangay.resident.model.Resident;
import com.barangay.barangay.resident.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final PeopleRepository peopleRepository;
    private final ComplainantRepository complainantRepository;
    private final RespondentRepository respondentRepository;
    private final WitnessRepository  witnessRepository;
    private final AuditLogService  auditLogService;




    @Transactional(readOnly = true)
    public List<PersonSearchResponseDTO> searchPeople(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return peopleRepository.searchPeopleForBlotter(query.trim());
    }

    @Transactional
    public void registerNewResident(ResidentRegistrationRequestDTO dto , User user , String ipAddress) {
        if (residentRepository.existsByBarangayIdNumber(dto.barangayIdNumber())) {
            throw new RuntimeException("Error: Barangay ID " + dto.barangayIdNumber() + " is already taken!");
        }

        People person = new People();
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
        person.setIsResident(true);

        Resident resident = new Resident();
        resident.setHouseholdNumber(dto.householdNumber());
        resident.setPrecinctNumber(dto.precinctNumber());
        resident.setIsVoter(dto.isVoter());
        resident.setIsHeadOfFamily(dto.isHeadOfFamily());
        resident.setOccupation(dto.occupation());
        resident.setCitizenship(dto.citizenship());
        resident.setReligion(dto.religion());
        resident.setBloodType(dto.bloodType());
        resident.setBarangayIdNumber(dto.barangayIdNumber());
        resident.setDateOfResidency(dto.dateOfResidency());

        resident.setPerson(person);
        person.setResident(resident);


        peopleRepository.save(person);


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
        Resident res = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found with ID: " + residentId));
        People p = res.getPerson();



        List<ResidentCaseHistoryDTO> history = new ArrayList<>();


        // As Complainant
        complainantRepository.findAllByPerson(p).forEach(c ->
                history.add(new ResidentCaseHistoryDTO(
                        c.getBlotterCase().getBlotterNumber(),
                        c.getBlotterCase().getIncidentDetail().getNatureOfComplaint().getName(),
                        "COMPLAINANT",
                        c.getBlotterCase().getStatus().name(),
                        c.getBlotterCase().getDateFiled()
                ))
        );

        // As Respondent
        respondentRepository.findAllByPerson(p).forEach(r ->
                history.add(new ResidentCaseHistoryDTO(
                        r.getBlotterCase().getBlotterNumber(),
                        r.getBlotterCase().getIncidentDetail().getNatureOfComplaint().getName(),
                        "RESPONDENT",
                        r.getBlotterCase().getStatus().name(),
                        r.getBlotterCase().getDateFiled()
                ))
        );

        // As Witness
        witnessRepository.findAllByPerson(p).forEach(w ->
                history.add(new ResidentCaseHistoryDTO(
                        w.getBlotterCase().getBlotterNumber(),
                        w.getBlotterCase().getIncidentDetail().getNatureOfComplaint().getName(),
                        "WITNESS",
                        w.getBlotterCase().getStatus().name(),
                        w.getBlotterCase().getDateFiled()
                ))
        );

        // Sort history by date (latest first)
        history.sort(Comparator.comparing(ResidentCaseHistoryDTO::dateFiled).reversed());

        // 4. CONSTRUCT THE COMPLETED DTO
        return new ResidentProfileViewDTO(
                // People Info
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

                res.getBarangayIdNumber(),
                res.getHouseholdNumber(),
                res.getPrecinctNumber(),
                res.getOccupation(),
                res.getCitizenship(),
                res.getReligion(),
                res.getBloodType(),
                res.getIsVoter(),
                res.getIsHeadOfFamily(),
                res.getDateOfResidency(),

                // Case History
                history
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
