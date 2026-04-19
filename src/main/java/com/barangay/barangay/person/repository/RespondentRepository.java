package com.barangay.barangay.person.repository;

import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Respondent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespondentRepository extends JpaRepository<Respondent, Long> {
    List<Respondent> findAllByPerson(Person person);

}
