package com.barangay.barangay.person.repository;

import com.barangay.barangay.person.dto.PersonSearchResponseDTO;
import com.barangay.barangay.person.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query("SELECT new com.barangay.barangay.person.dto.PersonSearchResponseDTO(" +
            "p.id, p.firstName, p.lastName, p.middleName, p.contactNumber, p.age, " +
            "p.birthDate, p.gender, p.civilStatus, p.email, p.completeAddress, " +
            "p.isResident, r.barangayIdNumber) " +
            "FROM Person p JOIN p.resident r " +
            "WHERE p.isResident = true " +
            "AND r.status = 'ACTIVE' " +
            "AND (" +
            "LOWER(p.firstName) LIKE LOWER(concat('%', :query, '%')) " +
            "OR LOWER(p.lastName) LIKE LOWER(concat('%', :query, '%')) " +
            "OR LOWER(r.barangayIdNumber) LIKE LOWER(concat('%', :query, '%'))" +
            ")")
    List<PersonSearchResponseDTO> searchPeopleForBlotter(@Param("query") String query);

    @Modifying
    @Query("DELETE FROM Person p WHERE p.id IN :ids AND p.isResident = false")
    void deleteNonResidentsByIds(@Param("ids") List<Long> ids);




    boolean existsByFirstNameAndLastNameAndMiddleNameAndBirthDateAndSuffixAndGender(
            String firstName,
            String lastName,
            String middleName,
            LocalDate birthDate,
            String suffix,
            String gender
    );
}
