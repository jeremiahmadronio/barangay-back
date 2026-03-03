package com.barangay.barangay.audit.service;
import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.enumerated.Severity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class AuditLogSpecification {

    private AuditLogSpecification() {}


    public static Specification<AuditLog> withFilters(
            String search,
            String severity,
            String module,
            String action
    ) {
        return Specification.allOf(
                hasSearch(search),
                hasSeverity(severity),
                hasModule(module),
                hasAction(action)
        );
    }


    private static Specification<AuditLog> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            Join<Object, Object> user = root.join("user", JoinType.LEFT);
            String pattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(user.get("firstName")),  pattern),
                    cb.like(cb.lower(user.get("lastName")),   pattern),
                    cb.like(cb.lower(root.get("reason")),     pattern),
                    cb.like(cb.lower(root.get("ipAddress")),  pattern)
            );
        };
    }

    private static Specification<AuditLog> hasSeverity(String severity) {
        return (root, query, cb) -> {
            if (severity == null || severity.isBlank()) return null;
            return cb.equal(root.get("severity"), Severity.valueOf(severity.toUpperCase()));
        };
    }

    private static Specification<AuditLog> hasModule(String module) {
        return (root, query, cb) -> {
            if (module == null || module.isBlank()) return null;
            return cb.equal(root.get("module"), module);
        };
    }

    private static Specification<AuditLog> hasAction(String action) {
        return (root, query, cb) -> {
            if (action == null || action.isBlank()) return null;
            return cb.equal(root.get("actionTaken"), action);
        };
    }
}