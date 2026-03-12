package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.Respondent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespondentRepository extends JpaRepository<Respondent, Long> {

}
