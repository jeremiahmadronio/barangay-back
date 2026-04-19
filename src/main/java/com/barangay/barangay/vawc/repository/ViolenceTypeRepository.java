package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.blotter.model.EvidenceType;
import com.barangay.barangay.vawc.model.ViolenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViolenceTypeRepository extends JpaRepository<ViolenceType,Long> {


    List<ViolenceType> findAllByIdIn(List<Long> ids);
    Optional<ViolenceType> findByName(String name);
}
