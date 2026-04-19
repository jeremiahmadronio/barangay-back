package com.barangay.barangay.clearance_management.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.ClearanceStatus;
import com.barangay.barangay.person.model.Person;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "issued_certificate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuedCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String certNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private CertificateTemplate template;

    @Column(length = 50)
    private String orNumber;

    @Column(length = 50)
    private String ctnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    private String requestorName;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode fieldValues;

    private boolean isFree = false;
    private boolean isArchive = false;
    private String archiveRemarks;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ClearanceStatus status;

    @CreationTimestamp
    private LocalDateTime issuedAt;

    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id")
    private User issuedById;

    @OneToOne(mappedBy = "issuedCertificate", cascade = CascadeType.ALL)
    private RevenueRecord revenueRecord;
}