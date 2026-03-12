package com.barangay.barangay.blotter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "people")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class People {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



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

    @Column
    private Short age;

    @Column(length = 20)
    private String gender;

    @Column(length = 50)
    private String civilStatus;

    @Column(length = 255)
    private String email;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}