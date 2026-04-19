package com.barangay.barangay.lupon.model;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.employee.model.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lupon_composition")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;


    @Column(length = 100, nullable = false,name = "case_position")
    private String position;


    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "assigned_at")
    private LocalDateTime assignedAt;
}
