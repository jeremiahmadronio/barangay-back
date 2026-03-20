package com.barangay.barangay.resident.repository;

import com.barangay.barangay.resident.dto.PersonSearchResponseDTO;
import com.barangay.barangay.resident.model.People;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeopleRepository extends JpaRepository<People, Long> {

    @Query("SELECT new com.barangay.barangay.resident.dto.PersonSearchResponseDTO(" +
            "p.id, p.firstName, p.lastName, p.middleName, p.contactNumber, p.age, " +
            "p.birthDate, p.gender, p.civilStatus, p.email, p.completeAddress, " +
            "p.isResident, r.barangayIdNumber) " +
            "FROM People p JOIN p.resident r " +
            "WHERE p.isResident = true AND (" +
            "LOWER(p.firstName) LIKE LOWER(concat('%', :query, '%')) " +
            "OR LOWER(p.lastName) LIKE LOWER(concat('%', :query, '%')) " +
            "OR LOWER(r.barangayIdNumber) LIKE LOWER(concat('%', :query, '%'))" +
            ")")
    List<PersonSearchResponseDTO> searchPeopleForBlotter(@Param("query") String query);
}
