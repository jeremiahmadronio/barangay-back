package com.barangay.barangay.blotter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "incident_frequencies")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class IncidentFrequency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false , unique = true)
    private String label;
}
