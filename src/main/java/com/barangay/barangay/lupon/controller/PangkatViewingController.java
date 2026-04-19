package com.barangay.barangay.lupon.controller;

import com.barangay.barangay.lupon.dto.LuponViewDTO;
import com.barangay.barangay.lupon.service.PangkatViewingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping("/api/v1/lupon-view")
@RequiredArgsConstructor
public class PangkatViewingController {

    private final PangkatViewingService pangkatViewingService;

    @GetMapping("/cases/{blotterNumber}")
    public ResponseEntity<LuponViewDTO> viewLuponCaseDetails(@PathVariable String blotterNumber) {

        LuponViewDTO caseView = pangkatViewingService.getLuponCaseView(blotterNumber);

        return ResponseEntity.ok(caseView);
    }
}
