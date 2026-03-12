package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.RelationshipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelationshipTypeRepository extends JpaRepository<RelationshipType, Long> {

    Optional<RelationshipType> findByName(String name);
    Optional<RelationshipType> findByNameIgnoreCase(String name);
}
