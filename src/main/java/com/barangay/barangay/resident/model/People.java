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
import java.time.Period;
import java.util.List;

@Entity
@Table(name = "people")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class People {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL)
    private Resident resident;

    @Lob
    private byte[] photo;

    @Column(length = 100, nullable = false)
    private String lastName;

    @Column(length = 100, nullable = false)
    private String firstName;

    @Column(length = 100)
    private String middleName;

    @Column(length = 15)
    private String contactNumber;

    @Column(columnDefinition = "TEXT")
    private String completeAddress;

    @Column(nullable = false)
    private Boolean isResident = false;

    @Column
    private Short age;

    private LocalDate birthDate;

    @Column(length = 20)
    private String gender;

    @Column(length = 50)
    private String civilStatus;

    @Column(length = 255)
    private String email;

    @Column(length = 100)
    private String occupation;


    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private List<Complainant> asComplainant;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private List<Respondent> asRespondent;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private List<Witness> asWitness;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}