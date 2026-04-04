package com.barangay.barangay.person.model;

import com.barangay.barangay.enumerated.ResidentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Person person;

    @Column(length = 50, name = "household_number")
    private String householdNumber;

    @Column(unique = true, name = "baranggay_id_number")
    private String barangayIdNumber;

    @Column(length = 20, name = "precinct_number")
    private String precinctNumber;

    @Column(nullable = false, name = "is_voter")
    private Boolean isVoter = false;

    @Column(nullable = false, name = "is_head_of_family")
    private Boolean isHeadOfFamily = false;


    @Column(nullable = false, name = "is_4ps")
    private Boolean is4ps = false;

    @Column(nullable = false, name = "is_pwd")
    private Boolean isPwd = false;

    @Column(length = 50, name = "pwd_id_number")
    private String pwdIdNumber;

    @Column(nullable = false, name = "is_indigent")
    private Boolean isIndigent = false;

    @Column(length = 50, name = "educational_attainment")
    private String educationalAttainment;

    @Column(length = 50, name = "citizenship")
    private String citizenship = "Filipino";

    @Column(length = 50,name = "religion")
    private String religion;

    @Column(length = 20, name = "blood_type")
    private String bloodType;


    @Column(name = "residency_date")
    private LocalDate dateOfResidency;

    @CreationTimestamp
    @Column(updatable = false, nullable = false,name = "created_at")
    private LocalDateTime createdDate;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ResidentStatus status = ResidentStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String statusRemarks;

    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResidentDocument> documents = new ArrayList<>();
}
