package com.barangay.barangay.blotter.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_follow_up")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class HearingFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Hearing hearing;

    @Column(columnDefinition = "TEXT", nullable = false,name = "remarks")
    @Convert(converter = EncryptedFieldConverter.class)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User recordedBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false,name = "created_at")
    private LocalDateTime createdAt;
}
