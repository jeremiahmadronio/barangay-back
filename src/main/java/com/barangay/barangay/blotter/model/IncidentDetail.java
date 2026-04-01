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

    @Column(name = "nature_of_complaint")
    private String natureOfComplaint;

    @Column(name = "incident_frequency")
    private String frequency;

    @Column(name = "incident_date")
    private LocalDate dateOfIncident;

    @Column(name = "incident_time")
    private LocalTime timeOfIncident;

    @Column(length = 255, name = "incident_location")
    private String placeOfIncident;

    @Column(columnDefinition = "TEXT",name = "injuries_damage_description")
    private String injuriesDamagesDescription;

}