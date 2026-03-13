package com.barangay.barangay.blotter.service;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BlotterRecordsSpecificationsFiltering {

    public static Specification<BlotterCase> buildFormalDocketFilter(
            String search, String status, Long natureId,
            LocalDate start, LocalDate end,
            Long departmentId, CaseType caseType) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();


            predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            predicates.add(cb.equal(root.get("caseType"), caseType));

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";

                var complainantJoin = root.join("complainant", JoinType.LEFT).join("person", JoinType.LEFT);
                var respondentJoin = root.join("respondent", JoinType.LEFT).join("person", JoinType.LEFT);

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("blotterNumber")), likePattern),
                        cb.like(cb.lower(complainantJoin.get("lastName")), likePattern),
                        cb.like(cb.lower(respondentJoin.get("lastName")), likePattern)
                ));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), CaseStatus.valueOf(status)));
            }

            if (natureId != null) {
                var incidentJoin = root.join("incidentDetail", JoinType.LEFT);
                predicates.add(cb.equal(incidentJoin.get("natureOfComplaint").get("id"), natureId));
            }

            if (start != null && end != null) {
                predicates.add(cb.between(
                        root.get("dateFiled"),
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}