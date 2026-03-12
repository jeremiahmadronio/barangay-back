package com.barangay.barangay.blotter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "incident_details")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class IncidentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private BlotterCase blotterCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nature_of_complaint_id")
    private NatureOfComplaint natureOfComplaint;

    @Column
    private LocalDate dateOfIncident;

    @Column
    private LocalTime timeOfIncident;

    @Column(length = 255)
    private String placeOfIncident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frequency_id")
    private IncidentFrequency frequency;

    @Column(columnDefinition = "TEXT")
    private String injuriesDamagesDescription;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
}