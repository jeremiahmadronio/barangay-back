package com.barangay.barangay.blotter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lupon_referral")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class LuponReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private BlotterCase blotterCase;

    @Column(name = "referred_at")
    private LocalDateTime referredAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;
    @Column(name = "extension_at")
    private LocalDateTime extensionAt;
    @Column(columnDefinition = "TEXT", name = "extension_reason")
    private String extensionReason;
    @Column(name = "extension_count")
    private Integer extensionCount = 0;
}
