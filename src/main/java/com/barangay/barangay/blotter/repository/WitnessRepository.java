package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.Witness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WitnessRepository extends JpaRepository<Witness,Long> {

    List<Witness> findAllByBlotterCase(BlotterCase blotterCase);
}
