package com.barangay.barangay.ftjs.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "first_time_job_seeker_notes")
@Data
public class FirstTimeJobSeekerNotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ftjs_id")
    private FirstTimeJobSeeker ftjs;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = EncryptedFieldConverter.class)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
