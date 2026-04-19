package com.barangay.barangay.clearance_management.repository;

import com.barangay.barangay.clearance_management.model.TemplateSignatory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateSignatoryRepository extends JpaRepository<TemplateSignatory, Long> {
}
