package com.barangay.barangay.lupon.repository;

import com.barangay.barangay.lupon.model.PangkatAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PangkatAttendanceRepository extends JpaRepository<PangkatAttendance, Long> {

    List<PangkatAttendance> findByHearingId(Long hearingId);
}
