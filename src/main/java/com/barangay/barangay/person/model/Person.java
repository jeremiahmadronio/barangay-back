package com.barangay.barangay.person.model;

import com.barangay.barangay.security.encryption_and_decryption.EncryptedFieldConverter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "person")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "photo")
    private byte[] photo;

    @Column(length = 100, nullable = false, name = "last_name")
    @JdbcTypeCode(Types.VARCHAR)
    private String lastName;

    @Column(length = 100, nullable = false,name = "first_name")
    @JdbcTypeCode(Types.VARCHAR)
    private String firstName;

    @Column(length = 100, name = "middle_name")
    private String middleName;

    @Column(length = 10)
    private String suffix;

    @Column(columnDefinition = "TEXT", name = "contact_number")
    @Convert(converter = EncryptedFieldConverter.class)
    private String contactNumber;

    @Column(columnDefinition = "TEXT", name = "complete_address")
    @Convert(converter = EncryptedFieldConverter.class)
    private String completeAddress;

    @Column(nullable = false, name = "is_resident")
    private Boolean isResident = false;

    @Column(name = "age")
    private Short age;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20 , name = "gender")
    private String gender;

    @Column(columnDefinition = "TEXT", name = "civil_status")
    private String civilStatus;

    @Column(length = 255 , name = "email")
    private String email;

    @Column(columnDefinition = "TEXT",name = "occupation")
    @Convert(converter = EncryptedFieldConverter.class)
    private String occupation;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Resident resident;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private List<Complainant> asComplainant;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private List<Respondent> asRespondent;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private List<Witness> asWitness;


}