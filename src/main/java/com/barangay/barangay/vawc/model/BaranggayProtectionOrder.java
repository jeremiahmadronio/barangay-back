package com.barangay.barangay.vawc.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.BpoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class BaranggayProtectionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "case_id" , nullable = false)
    private BlotterCase blotterCase;

    @Column(name = "bpo_control_number", unique = true, nullable = false, length = 100)
    private String bpoControlNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BpoStatus status = BpoStatus.PENDING;

    @Column(name = "expired_at")
    private LocalDate expiredAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "violence_type_bridge",
            joinColumns = @JoinColumn(name = "bpo_id"),
            inverseJoinColumns = @JoinColumn(name = "violence_type_id")
    )
    private Set<ViolenceType> violenceTypes = new HashSet<>();
}
