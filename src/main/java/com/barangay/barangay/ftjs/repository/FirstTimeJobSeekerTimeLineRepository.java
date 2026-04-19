package com.barangay.barangay.ftjs.repository;

import com.barangay.barangay.ftjs.model.FirstTimeJobSeekerTimeLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FirstTimeJobSeekerTimeLineRepository extends JpaRepository<FirstTimeJobSeekerTimeLine, Integer> {

    List<FirstTimeJobSeekerTimeLine> findAllByFtjsIdOrderByEventDateDesc (Long ftjsId);
}
