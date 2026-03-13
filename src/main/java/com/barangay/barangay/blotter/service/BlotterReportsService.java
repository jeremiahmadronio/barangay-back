package com.barangay.barangay.blotter.service;

import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlotterReportsService {

    private final BlotterCaseRepository  blotterCaseRepository;
}
