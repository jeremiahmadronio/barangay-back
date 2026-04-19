package com.barangay.barangay.ftjs.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.ftjs.dto.*;
import com.barangay.barangay.ftjs.service.FirstTimeJobSeekerService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ftjs")
@RequiredArgsConstructor
public class FirstTimeJobSeekerController {

    private final FirstTimeJobSeekerService firstTimeJobSeekerService;




    @PostMapping("/entry")
    public ResponseEntity<?> createRequest(
          @Valid @RequestBody FtjsRequestDTO dto ,
           @AuthenticationPrincipal CustomUserDetails actor,
           HttpServletRequest httpServletRequest
    ){
        String ipAddress = IpAddressUtils.getClientIp(httpServletRequest);
        firstTimeJobSeekerService.addRequest(dto,actor.user(),ipAddress);
        return  ResponseEntity.ok("Success full create request");
    }

    @PostMapping("/add-notes")
    public ResponseEntity<?> addNotes(
         @Valid @RequestBody   NotesRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest httpServletRequest
    ){
        String ipAddress = IpAddressUtils.getClientIp(httpServletRequest);
        firstTimeJobSeekerService.addNotes(dto,actor.user(),ipAddress);
        return  ResponseEntity.ok("Success full create notes");
    }

    @PostMapping("/request-new")
    public ResponseEntity<?> requestNewFtjs(
           @Valid @RequestBody RequestNewFtjsDTO dto,
          @AuthenticationPrincipal CustomUserDetails actor,
           HttpServletRequest httpServletRequest)
    {
        String ipAddress = IpAddressUtils.getClientIp(httpServletRequest);
        firstTimeJobSeekerService.RequestNewFtjs(dto,actor.user(),ipAddress);
        return  ResponseEntity.ok("Success issue new certificate");
    }

    @GetMapping("view-notes/{ftjsId}")
    public ResponseEntity<List<NotesResponseDTO>>getNotes(
           @PathVariable Long ftjsId
    ){
        return ResponseEntity.ok(firstTimeJobSeekerService.getNotesByFtjsId(ftjsId));
    }


    @GetMapping("/view-timeline/{ftjsId}")
    public ResponseEntity<List<TimelineResponseDTO>>getTimelines(
            @PathVariable Long ftjsId)
    {
        return ResponseEntity.ok(firstTimeJobSeekerService.getTimeline(ftjsId));
    }

    @GetMapping("/view-full-replacement/{replacementId}")
    public ResponseEntity<ResponseNewFtjsFullDetailsDTO> getNewFtjsFullDetailsDTO(
          @PathVariable  Long replacementId
    ){
        return ResponseEntity.ok(firstTimeJobSeekerService.getAffidavitDetails(replacementId));
    }


    @GetMapping("/view-sumarry/{ftjsId}")
    public ResponseEntity<List<ResponseNewFtjsSummaryDTO>> getFtjsSumarryDTO(
            @PathVariable  Long ftjsId
    ){
        return ResponseEntity.ok(firstTimeJobSeekerService.getAffidavitSummary(ftjsId));
    }

    @GetMapping("/view-full/{ftjsId}")
    public ResponseEntity<FtjsFullResponseDTO> getFtjsFullResponseDTO(
            @PathVariable  Long ftjsId
    ){
        return ResponseEntity.ok(firstTimeJobSeekerService.getFullDetails(ftjsId));
    }


    @GetMapping("/archive-table")
    public ResponseEntity<List<ArchiveTableResponseDTO>> getArchiveTable() {
        return ResponseEntity.ok(firstTimeJobSeekerService.getArchivedTableSummary());
    }

    @GetMapping("/summary")
    public ResponseEntity<List<FtjsTableDTO>> getSummaryTable(
           ) {
        List<FtjsTableDTO> summary = firstTimeJobSeekerService.getFtjsTableSummary();
        return ResponseEntity.ok(summary);
    }



    @GetMapping("/stats")
    public ResponseEntity<FtjsStatsResponseDTO> getFtjsStats(){
        return ResponseEntity.ok(firstTimeJobSeekerService.getFtjsStats());
    }

    @GetMapping("/archive-stats")
    public ResponseEntity<ArchiveResponseDTO> getFtjsArchiveStats(){
        return ResponseEntity.ok(firstTimeJobSeekerService.getArchiveStats());
    }

    @PatchMapping("edit/{id}")
    public ResponseEntity<String> updateFtjsRequest(
            @PathVariable Long id,
            @RequestBody FtjsEditRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        firstTimeJobSeekerService.updateRequest(id, dto, actor.user(), ipAddress);
        return ResponseEntity.ok("FTJS record updated successfully.");
    }


    @PatchMapping("/update-status/{id}")
    public ResponseEntity<String> updateFtjsStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        firstTimeJobSeekerService.toggleArchive(id, dto, actor.user(), ipAddress);
        return ResponseEntity.ok("Status successfully updated");
    }


}
