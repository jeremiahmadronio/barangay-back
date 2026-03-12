package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "blotter_cases")
@RequiredArgsConstructor
public class BlotterCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String blotterNumber;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseType caseType;

    @Column(nullable = false)
    private LocalDateTime dateFiled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status = CaseStatus.PENDING;

    @Column
    private Boolean isCertified = false;

    @Column
    private LocalDateTime certifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_officer_id")
    private User receivingOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Complainant complainant;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Respondent respondent;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IncidentDetail incidentDetail;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Narrative narrativeStatement;
}