package com.barangay.barangay.blotter.service;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BlotterRecordsSpecificationsFiltering {

    public static Specification<BlotterCase> buildFilter(
            String search, String status, Long natureId, LocalDate start, LocalDate end) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            var complainantJoin = root.join("complainant", JoinType.LEFT);
            var personJoin = complainantJoin.join("person", JoinType.LEFT);

            // Search (Blotter # or Name)
            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("blotterNumber")), likePattern),
                        cb.like(cb.lower(personJoin.get("lastName")), likePattern),
                        cb.like(cb.lower(personJoin.get("firstName")), likePattern)
                ));
            }

            // Status
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), CaseStatus.valueOf(status)));
            }

            // Nature ID
            if (natureId != null) {
                var incidentJoin = root.join("incidentDetail", JoinType.LEFT);
                var natureJoin = incidentJoin.join("natureOfComplaint", JoinType.LEFT);
                predicates.add(cb.equal(natureJoin.get("id"), natureId));
            }

            // Date Range
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


    public static Specification<BlotterCase> excludeStatus(
            String search, String statusToExclude, Long natureId, LocalDate start, LocalDate end) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (statusToExclude != null && !statusToExclude.isEmpty()) {
                predicates.add(cb.notEqual(root.get("status"), CaseStatus.valueOf(statusToExclude)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}