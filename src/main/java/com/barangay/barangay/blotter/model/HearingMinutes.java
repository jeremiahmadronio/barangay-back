package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.HearingOutcome;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hearing_minutes")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class HearingMinutes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id", nullable = false)
    private Hearing hearing;

    @Column
    private Boolean complainantPresent = false;

    @Column
    private Boolean respondentPresent = false;

    @Column(columnDefinition = "TEXT")
    private String hearingNotes;

    @Enumerated(EnumType.STRING)
    @Column
    private HearingOutcome outcome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @CreationTimestamp
    @Column(updatable = false   , nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}