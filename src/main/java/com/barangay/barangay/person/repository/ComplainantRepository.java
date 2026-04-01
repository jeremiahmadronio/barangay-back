package com.barangay.barangay.person.repository;

import com.barangay.barangay.person.model.Complainant;
import com.barangay.barangay.person.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplainantRepository extends JpaRepository<Complainant, Long> {
    List<Complainant> findAllByPerson(Person person);
}
