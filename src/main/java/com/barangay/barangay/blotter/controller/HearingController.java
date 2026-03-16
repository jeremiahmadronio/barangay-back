package com.barangay.barangay.blotter.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.blotter.dto.hearing.FollowUpHearingDTO;
import com.barangay.barangay.blotter.dto.hearing.HearingFullDetailsDTO;
import com.barangay.barangay.blotter.dto.hearing.RecordMinutesRequest;
import com.barangay.barangay.blotter.dto.hearing.ScheduleHearingRequest;
import com.barangay.barangay.blotter.service.HearingService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/hearing")
@RequiredArgsConstructor
public class HearingController {

    private final HearingService hearingService;


    @PostMapping("/schedule-hearing")
    public ResponseEntity<?> scheduleHearing(
            @Valid @RequestBody ScheduleHearingRequest dto,
            @AuthenticationPrincipal CustomUserDetails officer,
            HttpServletRequest request) {

        String ipAddress = IpAddressUtils.getClientIp(request);

        hearingService.scheduleNewHearing(dto, officer.user(), ipAddress);
        return ResponseEntity.ok("Successfully scheduled new hearing");
    }


    @PostMapping("/record-minutes")
    public ResponseEntity<?> recordHearingMinutes(
          @Valid @RequestBody  RecordMinutesRequest dto,
            @AuthenticationPrincipal CustomUserDetails officer,
            HttpServletRequest request ){

        String ipAddress = IpAddressUtils.getClientIp(request);
        hearingService.recordHearingMinutes(dto, officer.user(), ipAddress);
        return ResponseEntity.ok("Successfully recorded new hearing");
    }



    @PostMapping("/follow-up/{hearingId}")
    public ResponseEntity<?> recordHearingFollowUp(
            @PathVariable  Long hearingId,
            @Valid @RequestBody FollowUpHearingDTO dto ,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request
            ){
        String ipAddress = IpAddressUtils.getClientIp(request);
        hearingService.recordHearingFollowUp(hearingId ,dto,user.user(),ipAddress);
        return ResponseEntity.ok("Successfully follow up new hearing");

    }

        @GetMapping("/hearing-details/{hearingId}")
        public ResponseEntity<HearingFullDetailsDTO> hearingDetails (
                @PathVariable Long hearingId
        ){
            return ResponseEntity.ok(hearingService.getHearingFullDetails(hearingId));
        }

}