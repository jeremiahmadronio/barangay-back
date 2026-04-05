package com.barangay.barangay.vawc.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.blotter.dto.complaint.EvidenceOptionDTO;
import com.barangay.barangay.blotter.dto.notes.AddCaseNoteRequest;
import com.barangay.barangay.blotter.dto.notes.CaseNoteViewDTO;
import com.barangay.barangay.blotter.dto.reports_and_display.CaseTimeLineDTO;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.vawc.dto.*;
import com.barangay.barangay.vawc.service.ComplaintService;
import com.barangay.barangay.vawc.service.VawcService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vawc")
@RequiredArgsConstructor
public class VawcController {

    private final ComplaintService complaintService;
    private final VawcService vawcService;



    @PostMapping("/complaint-entry")
    public ResponseEntity<?> complaintEntry (
           @Valid @RequestBody ComplaintDTO dto,
          @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest httpRequest
    ){
        String ipAddress = IpAddressUtils.getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.fileVAWCComplaint(dto,actor.user(),ipAddress));

    }

    @GetMapping("/evidence-options")
    public ResponseEntity<List<EvidenceOptionDTO>> getEvidenceOptions() {
        return ResponseEntity.ok(complaintService.getEvidenceOptions());
    }


    @GetMapping("/violence-options")
    public ResponseEntity<List<ViolenceOptionDTO>> getOptions() {
        return ResponseEntity.ok(complaintService.getViolenceOptions());
    }

    @GetMapping("/case-summary")
    public ResponseEntity<Page<CaseSummaryDTO>> getVAWCCases(
            @AuthenticationPrincipal CustomUserDetails officer,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String violenceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Pageable pageable
    ) {
        Page<CaseSummaryDTO> summary = vawcService.getVAWCSummary(
                officer.user(),
                search,
                status,
                violenceType,
                dateFrom,
                dateTo,
                pageable
        );
        return ResponseEntity.ok(summary);
    }


    @GetMapping("/vawc-stats")
    public ResponseEntity<CaseStatsDTO> getStats() {
        return ResponseEntity.ok(vawcService.getVawcStats());
    }


    @GetMapping("/details/{id}")
    public ResponseEntity<CaseViewDTO> getCaseView(@PathVariable Long id) {
        return ResponseEntity.ok(vawcService.getVawcCaseDetails(id));
    }


    @PutMapping("/activate-bpo/{caseId}")
    public ResponseEntity<String> activateBpo(
            @PathVariable Long caseId,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        return ResponseEntity.ok(vawcService.activateBpo(caseId, actor.user(), ipAddress));
    }


    @GetMapping("/bpo-details/{caseId}")
    public ResponseEntity<BpoDetails> getBpoInfo(@PathVariable Long caseId) {
        return ResponseEntity.ok(vawcService.getActivatedBpoDetails(caseId));
    }

    @PostMapping("add-intervention")
    public ResponseEntity<?> addIntervention(
      @Valid @RequestBody InterventionRequestDTO dto,
       @AuthenticationPrincipal CustomUserDetails actor,
        HttpServletRequest httpRequest
    ){
        String ipAddress = IpAddressUtils.getClientIp(httpRequest);
        return ResponseEntity.ok(vawcService.addIntervention(dto,actor.user(),ipAddress));

    }

    @PostMapping("/follow-up")
    public ResponseEntity<String> addFollowUp(
            @RequestBody @Valid FollowUpDTO dto,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest httpRequest) {
        String ipAddress = IpAddressUtils.getClientIp(httpRequest);
        return ResponseEntity.ok(vawcService.addFollowUp(dto, actor.user(),ipAddress ));
    }



    @GetMapping("/intervention-details/{interventionId}")
    public ResponseEntity<InterventionViewDTO> getInterventionDetails(
           @PathVariable Long interventionId
    ){
        return ResponseEntity.ok(vawcService.getInterventionFullDetails(interventionId));
    }


    @GetMapping("/assign-officer-option")
    public ResponseEntity<List<AssignOfficerOptionDTO>> getAssignOfficerOption(){
        return ResponseEntity.ok(vawcService.getVawcInterventionDropdown());
    }


    @GetMapping("/assign-officer-complaint")
    public ResponseEntity<List<AssignOfficerOptionDTO>> getVawcComplaintOfficer(){
        return ResponseEntity.ok(vawcService.getVawcComplaintOfficer());
    }



    @PostMapping("/add-note")
    public ResponseEntity<?> addCaseNotes(
            @Valid @RequestBody AddCaseNoteRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){
        String ipAddress = IpAddressUtils.getClientIp(request);
        vawcService.addNoteToCase(dto, userDetails.user(), ipAddress);

        return ResponseEntity.ok("Successfully added note");
    }


    @GetMapping("/{caseId}/notes")
    public ResponseEntity<List<CaseNoteViewDTO>> getCaseNotes(@PathVariable Long caseId) {
        return ResponseEntity.ok(vawcService.getCaseNotesById(caseId));
    }



    @GetMapping("/timeline/{caseId}")
    public ResponseEntity<List<CaseTimeLineDTO>> getCaseTimeline(@PathVariable String caseId) {
        return ResponseEntity.ok(vawcService.getTimelineByCase(caseId));
    }

    @PutMapping("/cases/{id}/withdraw")
    public ResponseEntity<String> withdrawCase(
            @PathVariable Long id,
            @RequestBody UpdateCaseStatusDTO request) {
        return ResponseEntity.ok(vawcService.withdrawVawcCase(id, request));
    }


    @PostMapping("/create-referral")
    public ResponseEntity<String> issueReferral(
            @RequestBody CreateReferralDTO dto,
            @AuthenticationPrincipal CustomUserDetails actor) {

        String result = vawcService.issueVawcReferral(dto, actor.user());
        return ResponseEntity.ok(result);
    }


    @GetMapping("/cfa-detail/{caseId}")
    public ResponseEntity<DisplayCFADTO> getCfaDetail(@PathVariable Long caseId) {
        return ResponseEntity.ok(vawcService.getSingleCfaByCaseId(caseId));
    }

}
