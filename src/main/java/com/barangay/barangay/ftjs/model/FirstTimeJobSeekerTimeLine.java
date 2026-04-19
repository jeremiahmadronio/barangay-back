package com.barangay.barangay.ftjs.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.TimeLineType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "first_time_job_seeker_timeline")
@Data
public class FirstTimeJobSeekerTimeLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ftjs_id")
    private FirstTimeJobSeeker ftjs;

    @Enumerated(EnumType.STRING)
    private TimeLineType timelineType;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
