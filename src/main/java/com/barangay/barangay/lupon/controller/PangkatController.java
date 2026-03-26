package com.barangay.barangay.lupon.controller;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.lupon.dto.*;
import com.barangay.barangay.lupon.service.PangkatService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/lupon")
@RequiredArgsConstructor
public class PangkatController {

    private final PangkatService pangkatService;



    @GetMapping("/stats")
    public ResponseEntity<LuponCaseStatsDTO> getStats(@AuthenticationPrincipal CustomUserDetails officer) {
        return ResponseEntity.ok(pangkatService.getLuponDashboardStats(officer.user()));
    }

    @PatchMapping("/refer-to-lupon/{blotterNumber}")
    public ResponseEntity<?> referToLupon(
            @PathVariable String blotterNumber, // Changed from Long caseId to String
            @Valid @RequestBody ReferToLuponRequest referToLuponRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ){
        String ipAddress = IpAddressUtils.getClientIp(httpServletRequest);

        pangkatService.processLuponReferral(blotterNumber, referToLuponRequest, userDetails.user(), ipAddress);

        return ResponseEntity.ok("Case #" + blotterNumber + " has been successfully referred to Lupon.");
    }


    @GetMapping("/summary")
    public ResponseEntity<Page<LuponSummaryDTO>> getSummary(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long natureId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(pangkatService.getLuponSummary(search, natureId, startDate, endDate, page, size));
    }


    @PatchMapping("/extend/{caseId}")
    public ResponseEntity<?> extendPeriod(
            @PathVariable Long caseId,
            @Valid @RequestBody ExtendLuponRequest request,
            @AuthenticationPrincipal CustomUserDetails officer,
            HttpServletRequest servletRequest) {

        String ipAddress = IpAddressUtils.getClientIp(servletRequest);
        pangkatService.processExtension(caseId, request, officer.user(), ipAddress);
        return ResponseEntity.ok("Case period successfully extended by 15 days.");
    }



    //1
    @GetMapping("/hearing-view")
    public ResponseEntity<Page<HearingScheduleDTO>> getHearingSchedules(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "tab", defaultValue = "ALL") String tab,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Page<HearingScheduleDTO> hearings = pangkatService.getLuponHearingSchedules(search, tab, page, size);
        return ResponseEntity.ok(hearings);
    }



    @PutMapping("/new-status/{hearingId}")
    public ResponseEntity<String> updateHearingStatus(
            @PathVariable Long hearingId,
            @Valid @RequestBody UpdateHearingStatusDTO request,
            @AuthenticationPrincipal CustomUserDetails actor ,
            HttpServletRequest httpRequest) {

        String ipAddress = IpAddressUtils.getClientIp(httpRequest);

        pangkatService.updateHearingStatus(
                hearingId,
                request.newStatus(),
                request.remarks(),
                actor.user(),
                ipAddress
        );
        return ResponseEntity.ok("Hearing status successfully updated to " + request.newStatus());
    }


    @PostMapping("/{hearingId}/record-minutes")
    public ResponseEntity<String> recordMinutes(
            @PathVariable Long hearingId,
            @RequestBody RecordHearingRequestDTO requestDTO,
          @AuthenticationPrincipal  CustomUserDetails actor,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        pangkatService.recordHearingMinutes(hearingId, requestDTO, actor.user(), ipAddress);

        return ResponseEntity.ok("Hearing minutes and attendance successfully recorded.");
    }


    @GetMapping("/details/{hearingId}")
    public ResponseEntity<HearingMinutesViewingRequestDTO> hearingDetails(
            @PathVariable Long hearingId){

        HearingMinutesViewingRequestDTO details = pangkatService.getHearingFullDetails(hearingId);

        return ResponseEntity.ok(details);


    }


}
