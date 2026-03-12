package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidence_records")
@Getter
@Setter
@RequiredArgsConstructor
public class EvidenceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private BlotterCase blotterCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_type_id", nullable = false)
    private EvidenceType type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}