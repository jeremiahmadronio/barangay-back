package com.barangay.barangay.ftjs.repository;

import com.barangay.barangay.ftjs.model.FirstTimeJobSeeker;
import com.barangay.barangay.ftjs.model.FirstTimeJobSeekerAffidavitOfLoss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstTimeJobSeekerAffidavitOfLossRepository extends JpaRepository<FirstTimeJobSeekerAffidavitOfLoss,Long> {
    List<FirstTimeJobSeekerAffidavitOfLoss> findAllByFtjsIdOrderByCreatedAtDesc(Long ftjsId);
}
