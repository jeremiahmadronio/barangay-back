package com.barangay.barangay.lupon.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.EvidenceRecord;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.blotter.repository.EvidenceRecordRepository;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.lupon.dto.LuponCaseMemberHandlerDTO;
import com.barangay.barangay.lupon.dto.LuponViewDTO;
import com.barangay.barangay.lupon.model.PangkatComposition;
import com.barangay.barangay.lupon.repository.CaseRepository;
import com.barangay.barangay.lupon.repository.PangkatAttendanceRepository;
import com.barangay.barangay.lupon.repository.PangkatCompositionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PangkatViewingService {

    private final PangkatAttendanceRepository pangkatAttendanceRepository;
    private final PangkatCompositionRepository pangkatCompositionRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final CaseRepository caseRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;


    @Transactional(readOnly = true)
    public LuponViewDTO getLuponCaseView(String blotterNumber) {

        BlotterCase blotterCase = caseRepository.findByBlotterNumber(blotterNumber)
                .orElseThrow(() -> new EntityNotFoundException("Case not found with Blotter Number: " + blotterNumber));

        List<PangkatComposition> pangkatMembers = pangkatCompositionRepository.findByBlotterCaseId(blotterCase.getId());
        List<EvidenceRecord> evidences = evidenceRecordRepository.findByBlotterCaseId(blotterCase.getId());

        return mapToLuponViewDTO(blotterCase, pangkatMembers, evidences);
    }

    private LuponViewDTO mapToLuponViewDTO(BlotterCase bCase, List<PangkatComposition> pangkat, List<EvidenceRecord> evidences) {

        long daysRemaining = 0;
        if (bCase.getLuponReferral().getDeadline() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), bCase.getLuponReferral().getDeadline());
        }

        // Map Complainant Details (Null-safe)
        LuponViewDTO.PersonDTO complainantDTO = null;
        if (bCase.getComplainant() != null && bCase.getComplainant().getPerson() != null) {
            var p = bCase.getComplainant().getPerson();
            complainantDTO = new LuponViewDTO.PersonDTO(
                    p.getFirstName(), p.getLastName(), p.getMiddleName(),
                    p.getContactNumber(), p.getAge() != null ? p.getAge().intValue() : null,
                    p.getGender(), p.getCivilStatus(), p.getEmail(), p.getCompleteAddress()
            );
        }

        // Map Respondent Details (Null-safe)
        LuponViewDTO.RespondentDTO respondentDTO = null;
        if (bCase.getRespondent() != null) {
            var r = bCase.getRespondent();
            var p = r.getPerson();
            respondentDTO = new LuponViewDTO.RespondentDTO(
                    p != null ? p.getFirstName() : null,
                    p != null ? p.getLastName() : null,
                    p != null ? p.getMiddleName() : null,
                    r.getAlias(),
                    p != null ? p.getContactNumber() : null,
                    p != null && p.getAge() != null ? p.getAge().intValue() : null,
                    p != null ? p.getGender() : null,
                    p != null ? p.getBirthDate() : null,
                    p != null ? p.getCivilStatus() : null,
                    p != null ? p.getOccupation() : null,
                    r.getRelationshipToComplainant(),
                    p != null ? p.getCompleteAddress() : null,
                    r.getLivingWithComplainant() != null ? r.getLivingWithComplainant() : false
            );
        }

        // Map Incident Details (Null-safe)
        LuponViewDTO.IncidentDetailDTO incidentDTO = null;
        if (bCase.getIncidentDetail() != null) {
            var i = bCase.getIncidentDetail();
            incidentDTO = new LuponViewDTO.IncidentDetailDTO(
                    i.getNatureOfComplaint() != null ? i.getNatureOfComplaint() : null,
                    i.getDateOfIncident(),
                    i.getTimeOfIncident(),
                    i.getPlaceOfIncident(),
                    i.getFrequency() != null ? i.getFrequency() : null,
                    i.getInjuriesDamagesDescription()
            );
        }

        // Map Pangkat Members
        List<LuponCaseMemberHandlerDTO> memberDTOs = pangkat.stream()
                .map(p -> new LuponCaseMemberHandlerDTO(
                        p.getId(),
                        p.getEmployee().getPerson().getFirstName(),
                        p.getEmployee().getPerson().getLastName(),
                        p.getEmployee().getPosition()
                ))
                .collect(Collectors.toList());

        List<String> evidenceNames = evidences.stream()
                .map(e -> e.getType().getTypeName())
                .collect(Collectors.toList());

        List<WitnessDTO> witnessDTOs = bCase.getWitnesses().stream()
                .map(w -> {
                    var person = w.getPerson();

                    Long personId = (person != null) ? person.getId() : null;
                    String fullName = (person != null) ? person.getFirstName() + " " + person.getLastName() : "Unknown";
                    String contactNum = (person != null) ? person.getContactNumber() : null;
                    String address = (person != null) ? person.getCompleteAddress() : null;

                    return new WitnessDTO(
                            personId,
                            fullName,
                            contactNum,
                            address,
                            w.getTestimony()
                    );
                }).collect(Collectors.toList());

        String narrativeStr = null;
        if (bCase.getNarrativeStatement() != null) {
            narrativeStr = bCase.getNarrativeStatement().getStatement();
        }

        // Build Final Root DTO
        return new LuponViewDTO(
                bCase.getId(),
                bCase.getBlotterNumber(),
                bCase.getCaseType(),
                bCase.getStatus(),
                bCase.getStatusRemarks(),
                bCase.getDateFiled(),
                bCase.getLuponReferral().getReferredAt(),
                bCase.getCreatedBy() != null ? bCase.getCreatedBy().getUsername() : null, // Assuming User has username
                new LuponViewDTO.MediationInfoDTO(
                        bCase.getLuponReferral().getDeadline(), daysRemaining, bCase.getLuponReferral().getExtensionCount(),
                        bCase.getLuponReferral().getExtensionAt(), bCase.getLuponReferral().getExtensionReason(), bCase.getSettlementTerms()
                ),
                complainantDTO,
                respondentDTO,
                incidentDTO,
                narrativeStr,
                evidenceNames,
                witnessDTOs,
                memberDTOs
        );
    }
}
