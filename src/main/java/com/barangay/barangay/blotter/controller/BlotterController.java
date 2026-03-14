package com.barangay.barangay.blotter.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.blotter.dto.*;
import com.barangay.barangay.blotter.dto.Records.FtrSummaryStatsDTO;
import com.barangay.barangay.blotter.service.BlotterService;
import com.barangay.barangay.blotter.service.BlotterServiceViewOnly;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blotter")
@RequiredArgsConstructor
public class BlotterController {

  private final BlotterService  blotterService;
  private final BlotterServiceViewOnly  blotterServiceViewOnly;


    @PostMapping("/add-note")
    public ResponseEntity<?> addCaseNotes(
            @Valid @RequestBody AddCaseNoteRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){
        String ipAddress = IpAddressUtils.getClientIp(request);
          blotterService.addNoteToCase(dto, userDetails.user(), ipAddress);

        return ResponseEntity.ok("Successfully added note");
    }


    @GetMapping("/record-table")
    public ResponseEntity<Page<BlotterSummaryDTO>> getPagedBlotters(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long natureId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 10, sort = "dateFiled", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                blotterServiceViewOnly.getPagedBlotters(
                        userDetails.user(),
                        search,
                        status,
                        natureId,
                        start,
                        end,
                        pageable
                )
        );
    }

    @GetMapping("/docket-table")
    public ResponseEntity<Page<BlotterSummaryDTO>> getDocketTable(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long natureId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 10, sort = "dateFiled", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                blotterServiceViewOnly.docketTable(
                        userDetails.user(),
                        search,
                        status,
                        natureId,
                        start,
                        end,
                        pageable
                )
        );
    }


    @GetMapping("/view-all/{blotterNumber}")
    public ResponseEntity<BlotterRecordViewDTO> getFullBlotterRecord(
            @PathVariable String blotterNumber) {

        return ResponseEntity.ok(
                blotterServiceViewOnly.getFullBlotterRecord(blotterNumber)
        );
    }


    @GetMapping("/view-all-docket/{blotterNumber}")
    public ResponseEntity<BlotterDocketViewDTO> getFullBlotterDocket(
            @PathVariable String blotterNumber) {

        return ResponseEntity.ok(
                blotterServiceViewOnly.getDocketFullView(blotterNumber)
        );
    }


   @GetMapping("/mediation-process/{blotterNumber}")
    public ResponseEntity<MediationProcessDTO> getMediationProcess(
           @PathVariable String blotterNumber){

        return ResponseEntity.ok(blotterServiceViewOnly.getMediationProcess(blotterNumber));
    }

    @GetMapping("/hearing-view/{blotterNumber}")
  public ResponseEntity<List<HearingViewDTO>> getHearingView(
           @PathVariable String blotterNumber
  ){
        return ResponseEntity.ok(blotterServiceViewOnly.getCaseHearings(blotterNumber));
  }



    @GetMapping("/markers")
    public ResponseEntity<List<CalendarMarkerDTO>> getMarkers(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(blotterServiceViewOnly.getMonthMarkers(year, month));


    }

    @GetMapping("/busy-slots")
    public ResponseEntity<List<BusySlotDTO>> getBusySlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(blotterServiceViewOnly.getBusySlots(date));
    }


    @GetMapping("/hearing-minutes-view/{hearingId}")
    public ResponseEntity<MediationHearingViewDTO> getMediationHearingView(
            @PathVariable Long hearingId){
        return ResponseEntity.ok(blotterServiceViewOnly.getHearingView(hearingId));
    }



    @GetMapping("/{blotterNumber}/notes")
    public ResponseEntity<List<CaseNoteViewDTO>> getCaseNotes(@PathVariable String blotterNumber) {
        return ResponseEntity.ok(blotterServiceViewOnly.getCaseNotesByNumber(blotterNumber));
    }


    @GetMapping("/evidence-type-options")
    public ResponseEntity<List<EvidenceOptionDTO>> getEvidenceOptions() {
        return ResponseEntity.ok(blotterService.getEvidenceOptions());
    }


    @GetMapping("/nature-of-complaint-options")
    public ResponseEntity<List<NatureOptionDTO>> getNatureOptions() {
        return ResponseEntity.ok(blotterService.getNatureOptions());
    }


    @PutMapping("/update-case-status")
    public ResponseEntity<?> updateStatus(
            @RequestBody @Valid UpdateStatusDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){
        String ipAddress = IpAddressUtils.getClientIp(request);
        blotterService.updateStatus(dto,userDetails.user(),ipAddress);
        return ResponseEntity.ok("Status has been successfully updated");
    }

    @GetMapping("/docket-stats")
    public ResponseEntity<DocketStatsDTO> getDocketStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(blotterServiceViewOnly.getFormalStatsForUser(userDetails.user()));
    }

    @GetMapping("/records-stats")
    public ResponseEntity<FtrSummaryStatsDTO> getRecordsSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(blotterService.getFtrDashboardStats(userDetails.user()));
    }


}