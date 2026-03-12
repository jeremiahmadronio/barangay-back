package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.NatureOfComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NatureOfComplaintRepository extends JpaRepository<NatureOfComplaint, Long> {

    Optional<NatureOfComplaint> findByName(String name);

    List<NatureOfComplaint> findByNameInOrderByNameAsc(List<String> names);
}
