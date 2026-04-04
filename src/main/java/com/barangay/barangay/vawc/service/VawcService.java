package com.barangay.barangay.vawc.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.IncidentDetail;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Respondent;
import com.barangay.barangay.vawc.dto.CaseStatsDTO;
import com.barangay.barangay.vawc.dto.CaseSummaryDTO;
import com.barangay.barangay.vawc.dto.CaseViewDTO;
import com.barangay.barangay.vawc.dto.ViolenceTypeDTO;
import com.barangay.barangay.vawc.model.BaranggayProtectionOrder;
import com.barangay.barangay.vawc.repository.VawcCaseRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VawcService {


    private final VawcCaseRepository caseRepository;



    @Transactional(readOnly = true)
    public Page<CaseSummaryDTO> getVAWCSummary(
            User officer,
            String search, String status, String violenceType,
            LocalDate start, LocalDate end, Pageable pageable) {

        List<Long> deptIds = officer.getAllowedDepartments().stream()
                .map(Department::getId)
                .collect(Collectors.toCollection(ArrayList::new));

        if (deptIds.isEmpty()) {
            throw new RuntimeException("Unauthorized: No department assigned.");
        }

        Specification<BlotterCase> spec = VawcFilteringSpecs.buildVawcFilter(
                search, status, violenceType, start, end, deptIds
        );

        return caseRepository.findAll(spec, pageable).map(this::toListResponse);
    }


    public class VawcFilteringSpecs {
        public static Specification<BlotterCase> buildVawcFilter(
                String search, String status, String violenceType,
                LocalDate start, LocalDate end, List<Long> deptIds) {

            return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                predicates.add(root.get("department").get("id").in(deptIds));

                if (search != null && !search.isBlank()) {
                    String pattern = "%" + search.toLowerCase() + "%";
                    Predicate caseNum = cb.like(cb.lower(root.get("blotterNumber")), pattern);
                    Predicate firstName = cb.like(cb.lower(root.get("complainant").get("person").get("firstName")), pattern);
                    Predicate lastName = cb.like(cb.lower(root.get("complainant").get("person").get("lastName")), pattern);
                    predicates.add(cb.or(caseNum, firstName, lastName));
                }

                if (status != null && !status.isBlank()) {
                    try {
                        CaseStatus caseStatus = CaseStatus.valueOf(status.toUpperCase());
                        predicates.add(cb.equal(root.get("status"), caseStatus));
                    } catch (IllegalArgumentException ignored) {}
                }

                if (violenceType != null && !violenceType.isBlank()) {
                    String pattern = "%" + violenceType.toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("incidentDetail").get("natureOfComplaint")), pattern));
                }

                if (start != null && end != null) {
                    LocalDateTime from = start.atStartOfDay();
                    LocalDateTime to = end.atTime(23, 59, 59);
                    predicates.add(cb.between(root.get("dateFiled"), from, to));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }
    }

    private CaseSummaryDTO toListResponse(BlotterCase bc) {
        String victim = "Unknown Victim";
        if (bc.getComplainant() != null && bc.getComplainant().getPerson() != null) {
            Person p = bc.getComplainant().getPerson();
            String prefix = getPrefix(p.getGender(), p.getCivilStatus());
            victim = (prefix.isEmpty() ? "" : prefix + " ")
                    + p.getFirstName() + " " + p.getLastName();
        }

        String violenceTypes = "None";
        if (bc.getIncidentDetail() != null && bc.getIncidentDetail().getNatureOfComplaint() != null) {
            violenceTypes = bc.getIncidentDetail().getNatureOfComplaint();
        }

        String assignedOfficer = "Unassigned";
        if (bc.getEmployee() != null && bc.getEmployee().getPerson() != null) {
            Person ep = bc.getEmployee().getPerson();
            assignedOfficer = ep.getFirstName() + " " + ep.getLastName();
        }

        return new CaseSummaryDTO(
                bc.getId(),
                bc.getBlotterNumber(),
                victim,
                violenceTypes,
                bc.getStatus().name(),
                bc.getDateFiled(),
                assignedOfficer
        );
    }

    private String getPrefix(String gender, String civilStatus) {
        if (gender == null) return "";
        if (gender.equalsIgnoreCase("Male")) return "Mr.";
        if (civilStatus != null && civilStatus.equalsIgnoreCase("Married")) return "Mrs.";
        return "Ms.";
    }



    @Transactional(readOnly = true)
    public CaseStatsDTO getVawcStats() {
        long total = caseRepository.countTotalVawc();
        long pending = caseRepository.countByStatus(CaseStatus.PENDING);

        long active = total - caseRepository.countByStatus(CaseStatus.CLOSED)
                - caseRepository.countByStatus(CaseStatus.SETTLED);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenDaysAgo = now.minusDays(15);
        LocalDateTime twelveDaysAgo = now.minusDays(12);

        long expiringSoon = caseRepository.countExpiringBPOs(fifteenDaysAgo, twelveDaysAgo);

        return new CaseStatsDTO(total, active, expiringSoon, pending);
    }

    @Transactional(readOnly = true)
    public CaseViewDTO getVawcCaseDetails(Long id) {
        BlotterCase bc = caseRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new RuntimeException("Case with ID " + id + " not found."));

        BaranggayProtectionOrder bpo = caseRepository.findBpoWithViolenceTypes(id).orElse(null);
        List<String> evidenceNames = caseRepository.findEvidenceNamesByCaseId(id);

        String remainingTime;
        LocalDate bpoDeadline;

        if (bc.getStatus() == CaseStatus.PENDING) {
            LocalDateTime deadline24h = bc.getDateFiled().plusHours(24);
            long hours = java.time.Duration.between(LocalDateTime.now(), deadline24h).toHours();
            remainingTime = Math.max(0, hours) + "h";
            bpoDeadline = deadline24h.toLocalDate();
        }
        else {
            bpoDeadline = (bpo != null && bpo.getExpiredAt() != null)
                    ? bpo.getExpiredAt()
                    : bc.getDateFiled().toLocalDate().plusDays(15);
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), bpoDeadline);
            remainingTime = Math.max(0, days) + "d";
        }

        Person vP = bc.getComplainant().getPerson();
        Respondent res = bc.getRespondent();
        Person rP = res.getPerson();
        IncidentDetail idet = bc.getIncidentDetail();

        return new CaseViewDTO(
                bpoDeadline,
                remainingTime,
                bc.getBlotterNumber(),
                bc.getStatus(),
                bc.getStatusRemarks(),
                bc.getDateFiled(),
                bc.getEmployee() != null ? bc.getEmployee().getPerson().getFirstName() + " " + bc.getEmployee().getPerson().getLastName() : "Unassigned",
                bc.getCreatedBy() != null ? bc.getCreatedBy().getPerson().getFirstName() + " " + bc.getCreatedBy().getPerson().getLastName() : "System",

                vP.getFirstName(), vP.getLastName(), vP.getMiddleName(),
                vP.getContactNumber(), vP.getAge(), vP.getGender(),
                vP.getCivilStatus(), vP.getEmail(), vP.getCompleteAddress(),

                rP.getFirstName(), rP.getLastName(), rP.getMiddleName(),
                res.getAlias(), rP.getContactNumber(), (int) rP.getAge(),
                rP.getGender(), rP.getCivilStatus(), rP.getOccupation(),
                res.getRelationshipToComplainant(), rP.getCompleteAddress(),
                res.getLivingWithComplainant(),

                idet != null ? idet.getNatureOfComplaint() : "N/A",
                idet != null ? idet.getDateOfIncident() : null,
                idet != null ? idet.getTimeOfIncident() : null,
                idet != null ? idet.getPlaceOfIncident() : "N/A",
                idet != null ? idet.getFrequency() : "N/A",
                idet != null ? idet.getInjuriesDamagesDescription() : "N/A",

                bc.getNarrativeStatement() != null ? bc.getNarrativeStatement().getStatement() : "No narrative recorded.",
                evidenceNames,

                bc.getWitnesses().stream()
                        .filter(w -> w.getPerson() != null)
                        .map(w -> new WitnessDTO(
                                w.getPerson().getId(),
                                w.getPerson().getFirstName() + " " + w.getPerson().getLastName(),
                                w.getPerson().getContactNumber(),
                                w.getPerson().getCompleteAddress(),
                                w.getTestimony()
                        )).toList(),

                bpo != null ? bpo.getViolenceTypes().stream()
                        .map(v -> new ViolenceTypeDTO(
                                v.getId(),
                                v.getName(),
                                v.getDescription()
                        )).toList() : List.of()
        );
    }
}
