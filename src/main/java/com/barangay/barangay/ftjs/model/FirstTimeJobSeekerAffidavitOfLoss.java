package com.barangay.barangay.ftjs.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Resident;
import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "first_time_job_seeker_affidavit_of_loss")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirstTimeJobSeekerAffidavitOfLoss {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ftjs_id")
    private FirstTimeJobSeeker ftjs;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = EncryptedFieldConverter.class)
    private String reason;

    private LocalDate dateOfLoss;

    private Integer issuanceNumber;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] affidavitFiles;

    private BigDecimal amountPaid;

    @Column(name = "or_number",columnDefinition = "TEXT")
    @Convert(converter = EncryptedFieldConverter.class)
    private String orNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
