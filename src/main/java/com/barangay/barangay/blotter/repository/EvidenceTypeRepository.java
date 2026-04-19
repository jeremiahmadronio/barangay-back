package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.EvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceTypeRepository extends JpaRepository<EvidenceType, Long> {

    Optional<EvidenceType> findByTypeName(String typeName);


    List<EvidenceType> findByTypeNameInOrderByTypeNameAsc(List<String> names);

    List<EvidenceType> findAllByIdIn(List<Long> ids);
}
