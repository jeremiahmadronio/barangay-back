package com.barangay.barangay.blotter.model;

import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
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

    @Column(name = "nature_of_complaint" ,columnDefinition = "TEXT")
    private String natureOfComplaint;

    @Column(name = "incident_frequency")
    private String frequency;

    @Column(name = "incident_date")
    private LocalDate dateOfIncident;

    @Column(name = "incident_time")
    private LocalTime timeOfIncident;

    @Column(columnDefinition = "TEXT", name = "incident_location")
    @Convert(converter = EncryptedFieldConverter.class)
    private String placeOfIncident;

    @Column(columnDefinition = "TEXT",name = "injuries_damage_description")
    @Convert(converter = EncryptedFieldConverter.class)
    private String injuriesDamagesDescription;

}