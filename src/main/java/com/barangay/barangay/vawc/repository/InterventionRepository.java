package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.vawc.model.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention,Long> {

    @Query("""
        SELECT i FROM Intervention i
        LEFT JOIN FETCH i.performedBy p LEFT JOIN FETCH p.person
        LEFT JOIN FETCH i.createdBy u LEFT JOIN FETCH u.person
        WHERE i.id = :id
    """)
    Optional<Intervention> findByIdWithDetails(@Param("id") Long id);

}
