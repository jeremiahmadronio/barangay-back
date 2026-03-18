package com.barangay.barangay.resident.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resident")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long residentId;


    @OneToOne
    @JoinColumn(name = "person_id", nullable = false)
    private People person;

    @Column(length = 50)
    private String householdNumber;

    @Column(length = 20)
    private String precinctNumber;

    @Column(nullable = false)
    private Boolean isVoter = false;

    @Column(nullable = false)
    private Boolean isHeadOfFamily = false;

    @Column(length = 100)
    private String occupation;

    @Column(length = 50)
    private String citizenship = "Filipino";

    @Column(length = 50)
    private String religion;

    @Column(length = 20)
    private String bloodType;

    @Column(unique = true)
    private String barangayIdNumber;

    private LocalDate dateOfResidency;

    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
