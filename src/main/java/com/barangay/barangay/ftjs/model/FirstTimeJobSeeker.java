package com.barangay.barangay.ftjs.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.FtjsStatus;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Resident;
import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "first_time_job_seeker")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirstTimeJobSeeker{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;
    @Column(name = "ftjs_number", columnDefinition = "TEXT")
    private String ftjsNumber;

    @Convert(converter = EncryptedFieldConverter.class)
    @Column(name = "educational_attainment", columnDefinition = "TEXT")
    private String educationalAttainment;


    @Convert(converter = EncryptedFieldConverter.class)
    @Column(columnDefinition = "TEXT")
    private String course;

    @Convert(converter = EncryptedFieldConverter.class)
    @Column(name = "school_institution",columnDefinition = "TEXT")
    private String schoolInstitution;

    @Convert(converter = EncryptedFieldConverter.class)
    @Column(columnDefinition = "TEXT")
    private String schoolAddress;

    @Column(name = "valid_id_type")
    private String validIdType;

    @Convert(converter = EncryptedFieldConverter.class)
    @Column(name = "id_number", columnDefinition = "TEXT")
    private String idNumber;

    @Convert(converter = EncryptedFieldConverter.class)
    @Column(columnDefinition = "TEXT")
    private String purposeDocuments;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "oath_files")
    private byte[] oathFiles;

    @Column(name = "issuance_count")
    Integer issuanceCount;


    private LocalDate dateSubmitted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FtjsStatus status;

    private boolean isArchive;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = EncryptedFieldConverter.class)
    private String archiveRemarks;

    @Column(columnDefinition = "TEXT")
    private String statusRemarks;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
