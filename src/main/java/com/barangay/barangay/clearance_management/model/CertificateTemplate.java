package com.barangay.barangay.clearance_management.model;

import com.barangay.barangay.admin_management.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "certificate_template")
@Data
@AllArgsConstructor @NoArgsConstructor
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String certTitle;

    @Column(nullable = false, length = 20)
    private String layoutStyle;

    @Column(columnDefinition = "TEXT")
    private String certTagline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode bodySections;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode issueFields;

    private boolean requiresPhoto = false;
    private boolean requiresThumbmark = false;

    private boolean hasFee = false;

    private boolean hasCtn = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal certFee = BigDecimal.ZERO;


    private boolean hasArchive = false;
    private String archiveReason;

    private Integer validityMonths = 6;

    @Column(length = 80)
    private String footerText = "Not valid without dry seal.";

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<TemplateSignatory> signatories;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<IssuedCertificate> issuedCertificates;
}
