package com.barangay.barangay.lupon.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
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
    @JoinColumn(name = "_case_id", nullable = false, unique = true)
    private BlotterCase blotterCase;

    @Column(columnDefinition = "TEXT", nullable = false,name = "grounds")
    @Convert(converter = EncryptedFieldConverter.class)
    private String grounds;

    @Column(nullable = false,name = "subject_of_litigations")
    private String subjectOfLitigation;

    @Column(length = 50, unique = true, name = "control_number")
    private String controlNumber;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User issuedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;


    @CreationTimestamp
    @Column(updatable = false,name = "created_at")
    private LocalDateTime issuedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;





}
