package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.HearingOutcome;
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
@Table(name = "session_minutes")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class HearingMinutes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Hearing hearing;

    @Column(name = "is_complainant_present")
    private Boolean complainantPresent = false;

    @Column(name = "is_respondent_present")
    private Boolean respondentPresent = false;

    @Column(columnDefinition = "TEXT",name = "session_notes")
    @Convert(converter = EncryptedFieldConverter.class)
    private String hearingNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private HearingOutcome outcome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User recordedBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "created_at")
    private LocalDateTime createdAt;


}