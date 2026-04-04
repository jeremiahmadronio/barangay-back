package com.barangay.barangay.vawc.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.employee.model.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bpo_intervention_logs")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bpo_id", nullable = false)
    private BaranggayProtectionOrder baranggayProtectionOrder;

    @Column(name = "activity_type", length = 200, nullable = false)
    private String activityType;

    @Column(name = "intervention_details", columnDefinition = "TEXT")
    private String interventionDetails;


    @Column(name = "intervention_date", nullable = false)
    private LocalDateTime interventionDate;

    @Column(name = "intervention_duration")
    private Integer interventionDuration;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "intervention_performed_by",
            joinColumns = @JoinColumn(name = "intervention_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> performedBy = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


}
