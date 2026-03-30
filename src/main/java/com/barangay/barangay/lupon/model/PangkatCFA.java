package com.barangay.barangay.lupon.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.blotter.model.BlotterCase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pangkat_cfa")
@AllArgsConstructor @NoArgsConstructor
@Getter
@Setter
public class PangkatCFA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blotter_case_id", nullable = false, unique = true)
    private BlotterCase blotterCase;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String grounds;

    @Column(nullable = false)
    private String subjectOfLitigation;

    @Column(length = 50, unique = true)
    private String controlNumber;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime issuedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id")
    private User issuedBy;


    @UpdateTimestamp

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;



}
