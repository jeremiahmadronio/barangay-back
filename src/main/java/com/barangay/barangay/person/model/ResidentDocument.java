package com.barangay.barangay.person.model;

import com.barangay.barangay.enumerated.ResidentDocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resident_documents")
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class ResidentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(nullable = false, name = "document_name")
    private String documentName;

    @Column(nullable = false, name = "document_type")
    private String documentType;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResidentDocumentStatus status;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

}
