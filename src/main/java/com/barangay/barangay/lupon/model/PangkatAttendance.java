package com.barangay.barangay.lupon.model;

import com.barangay.barangay.blotter.model.Hearing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="lupon_attendance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PangkatAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Hearing hearing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lupon_composition_id", nullable = false)
    private PangkatComposition pangkatMember;

        @Column(nullable = false, name = "is_present")
    private Boolean isPresent = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "created_at")
    private LocalDateTime createdAt;


}
