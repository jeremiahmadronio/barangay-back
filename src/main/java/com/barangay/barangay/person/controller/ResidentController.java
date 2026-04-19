package com.barangay.barangay.person.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.person.dto.*;
import com.barangay.barangay.employee.service.EmployeeService;
import com.barangay.barangay.person.service.ResidentService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/resident")
public class ResidentController {

    private final ResidentService residentService;
    private final EmployeeService employeeService;



    @GetMapping("/stats")
    public ResponseEntity<ResidentStatsDTO> getResidentStats() {
        ResidentStatsDTO stats = residentService.getResidentDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerResident(
            @Valid @RequestBody ResidentRegistrationRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request
            ) {

        String ipAddress = IpAddressUtils.getClientIp(request);
        residentService.registerNewResident(dto,actor.user(),ipAddress);
        return ResponseEntity.ok("Successfully registered resident");

    }

    @GetMapping("/search")
    public ResponseEntity<List<PersonSearchResponseDTO>> search(@RequestParam String query) {
        List<PersonSearchResponseDTO> results = residentService.searchPeople(query);
        return ResponseEntity.ok(results);
    }


    @GetMapping("/resident-profile/{residentId}")
    public ResponseEntity<ResidentProfileViewDTO> displayResidentProfile (@Valid @PathVariable  Long residentId
    )
    {
        return ResponseEntity.ok(residentService.getFullResidentProfile(residentId));
    }

    @GetMapping("/table")
    public ResponseEntity<List<ResidentSummary>> getResidentTable(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Boolean isVoter,
            @RequestParam(required = false) String household) {
        return ResponseEntity.ok(residentService.getResidentTable(search, gender, isVoter, household));
    }

    @GetMapping("/lupon-employee")
    public ResponseEntity<List<EmployeeResponseDTO>> displayLuponEmployee(){
        return ResponseEntity.ok(employeeService.getLuponOfficialsPool());
    }


    @PatchMapping("update-resident/{residentId}")
    public ResponseEntity<?> updateResidentProfile(
           @PathVariable Long residentId,
           @Valid @RequestBody UpdateResidentProfileDTO dto,
          @AuthenticationPrincipal  CustomUserDetails actor,
           HttpServletRequest request
    ){
        String ipAddress = IpAddressUtils.getClientIp(request);
        residentService.updateResidentProfile(residentId,dto,actor.user(),ipAddress);
        return ResponseEntity.ok("Successfully updated resident");
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ResidentSuggestionsDTO> getSuggestions() {
        ResidentSuggestionsDTO suggestions = residentService.getResidentSuggestions();
        return ResponseEntity.ok(suggestions);
    }


    @GetMapping("/resident-full-profile/{residentId}")
    public ResponseEntity<ResidentFullProfileViewDTO> displayFullResident (@Valid @PathVariable  Long residentId
    )
    {
        return ResponseEntity.ok(residentService.getFullResidentProfileAdmin(residentId));
    }


    @PutMapping("/update-status/{residentId}")
    public ResponseEntity<?> archiveResident(
            @PathVariable Long residentId,
            @Valid @RequestBody UpdateStatusDTO request,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        residentService.updateResidentStatus(residentId, request, actor.user(), ipAddress);
        return ResponseEntity.ok("Resident status updated to " + request.status() + " successfully.");
    }


    @GetMapping("/archive/stats")
    public ResponseEntity<ArchiveStatsDTO> getArchiveStats() {
        return ResponseEntity.ok(residentService.getArchiveStats());
    }

}
