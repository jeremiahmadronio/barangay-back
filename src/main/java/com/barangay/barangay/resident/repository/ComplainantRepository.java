package com.barangay.barangay.resident.repository;

import com.barangay.barangay.resident.model.Complainant;
import com.barangay.barangay.resident.model.People;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplainantRepository extends JpaRepository<Complainant, Long> {
    List<Complainant> findAllByPerson(People person);
}
