package com.barangay.barangay.audit.model;

import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.auth.model.Department;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user connection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // department connection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    // basic column
    @Column(length = 45)
    private String ipAddress;
    private String module;

    @Enumerated(EnumType.STRING)
    private Severity severity;
    @Column(name = "action_taken")
    private String actionTaken;
    @Column(columnDefinition = "TEXT")
    private String reason;

    //json
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    //date related
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}