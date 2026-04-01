package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.LuponReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LuponReferralRepository extends JpaRepository<LuponReferral,Long> {
}
