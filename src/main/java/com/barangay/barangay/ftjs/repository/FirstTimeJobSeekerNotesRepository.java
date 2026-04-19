package com.barangay.barangay.ftjs.repository;

import com.barangay.barangay.ftjs.model.FirstTimeJobSeekerNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstTimeJobSeekerNotesRepository extends JpaRepository<FirstTimeJobSeekerNotes,Long> {
    List<FirstTimeJobSeekerNotes> findAllByFtjsIdOrderByCreatedAtDesc(Long ftjsId);
}
