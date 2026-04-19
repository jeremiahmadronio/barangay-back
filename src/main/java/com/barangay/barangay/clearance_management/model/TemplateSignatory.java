package com.barangay.barangay.clearance_management.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_signatory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSignatory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonBackReference
    private CertificateTemplate template;

    private String signatoryName;

    @Column(length = 100)
    private String signatoryTitle;
}