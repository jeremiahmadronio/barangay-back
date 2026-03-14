package com.barangay.barangay.lupon.model;

import com.barangay.barangay.blotter.model.BlotterCase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pangkat_composition")
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class PangkatComposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private BlotterCase blotterCase;

    @Column(length = 100, nullable = false)
    private String firstName;

    @Column(length = 100, nullable = false)
    private String lastName;

    @Column(length = 100, nullable = false)
    private String position;
    // "Pangkat Chairman etc"


    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime assignedAt;
}
