package com.barangay.barangay.resident.repository;

import com.barangay.barangay.resident.model.People;
import com.barangay.barangay.resident.model.Respondent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespondentRepository extends JpaRepository<Respondent, Long> {
    List<Respondent> findAllByPerson(People person);

}
