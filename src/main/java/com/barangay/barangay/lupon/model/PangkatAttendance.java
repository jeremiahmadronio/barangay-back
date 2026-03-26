package com.barangay.barangay.lupon.model;

import com.barangay.barangay.blotter.model.Hearing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="pangkat_attendance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PangkatAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id", nullable = false)
    private Hearing hearing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pangkat_member_id", nullable = false)
    private PangkatComposition pangkatMember;

        @Column(nullable = false)
    private Boolean isPresent = false;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;


}
