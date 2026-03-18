package com.barangay.barangay.resident.model;

import com.barangay.barangay.blotter.model.BlotterCase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "witnesses")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Witness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private BlotterCase blotterCase;

   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private People person;

    @Column(columnDefinition = "TEXT")
    private String testimony;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
}