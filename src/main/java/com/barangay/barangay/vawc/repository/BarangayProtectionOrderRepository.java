package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.vawc.model.BaranggayProtectionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarangayProtectionOrderRepository extends JpaRepository<BaranggayProtectionOrder, Long> {
}
