package com.barangay.barangay.blotter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nature_of_complaints")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NatureOfComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, unique = true, nullable = false)
    private String name;
}