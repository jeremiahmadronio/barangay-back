package com.barangay.barangay.person.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "respondents")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Respondent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;


    @Column(length = 100, name = "alias")
    private String alias;


    @Column(name = "relationship_to_complinant")
    private String relationshipToComplainant;

    @Column(name = "is_living_with_complainant")
    private Boolean livingWithComplainant = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}