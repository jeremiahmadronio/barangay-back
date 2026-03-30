package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import com.barangay.barangay.lupon.model.PangkatCFA;
import com.barangay.barangay.resident.model.Complainant;
import com.barangay.barangay.resident.model.Respondent;
import com.barangay.barangay.resident.model.Witness;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "blotter_cases")
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(columnDefinition = "TEXT")
    private String statusRemarks;





    @Column
    private Boolean isCertified = false;

    @Column
    private LocalDateTime certifiedAt;


    @Column(columnDefinition = "TEXT")
    private String settlementTerms;

    @Column
    private LocalDateTime settledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_officer_id")
    private User receivingOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column
    private LocalDateTime referredToLuponAt;

    @Column
    private LocalDateTime luponDeadline;

    @Column
    private LocalDateTime extensionDate;

    @Column(columnDefinition = "TEXT")
    private String extensionReason;

    @Column
    private Integer extensionCount = 0;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Complainant complainant;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Respondent respondent;

    @OneToMany(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Witness> witnesses = new ArrayList<>();

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IncidentDetail incidentDetail;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Narrative narrativeStatement;

    @OneToOne(mappedBy = "blotterCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PangkatCFA pangkatCfa;




}