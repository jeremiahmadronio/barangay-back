package com.barangay.barangay.clearance_management.model;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenue_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_cert_id", nullable = false)
    private IssuedCertificate issuedCertificate;

    @Column(unique = true, nullable = false,columnDefinition = "TEXT")
    @Convert(converter = EncryptedFieldConverter.class)
    private String orNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by_id")
    private User collectedById;
}