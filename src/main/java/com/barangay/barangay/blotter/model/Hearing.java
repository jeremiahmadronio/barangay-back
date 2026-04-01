package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.HearingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "case_session",
        uniqueConstraints = @UniqueConstraint(columnNames = {"case_id", "summon_number"})
)
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private BlotterCase blotterCase;

    @Column(nullable = false, name = "summon_number")
    private Short summonNumber;


    @Column(nullable = false, name = "session_start")
    private LocalDateTime scheduledStart;


    @Column(nullable = false, name = "session_end")
    private LocalDateTime scheduledEnd;


    @Column(length = 255, name = "venue")
    private String venue = "Barangay Hall";

    @Column(columnDefinition = "TEXT", name = "additional_notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status")
    private HearingStatus status = HearingStatus.SCHEDULED;


    @Column(name = "paanyaya_generated_at")
    private LocalDateTime paanyayaGeneratedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "created_at")
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "hearing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<HearingFollowUp> followUps = new ArrayList<>();

}