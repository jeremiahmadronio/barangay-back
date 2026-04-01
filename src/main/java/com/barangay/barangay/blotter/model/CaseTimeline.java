package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.TimelineEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "case_timeline")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CaseTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private BlotterCase blotterCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , name = "timeline_type")
    private TimelineEventType eventType;


    @Column(length = 255, nullable = false, name = "title")
    private String title;

    @Column(columnDefinition = "TEXT", name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User performedBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "event_date")
    private LocalDateTime eventDate;
}