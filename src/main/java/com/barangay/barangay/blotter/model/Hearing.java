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

@Entity
@Table(
        name = "hearings",
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

    @Column(nullable = false)
    private Short summonNumber;


    @Column(nullable = false)
    private LocalDateTime scheduledStart;


    @Column(nullable = false)
    private LocalDateTime scheduledEnd;


    @Column(length = 255)
    private String venue = "Barangay Hall";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column
    private HearingStatus status = HearingStatus.SCHEDULED;



    @Column
    private LocalDateTime paanyayaGeneratedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}