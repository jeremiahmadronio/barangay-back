package com.barangay.barangay.lupon.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.lupon.dto.CFA.CFARequest;
import com.barangay.barangay.lupon.dto.CFA.CFAResponse;
import com.barangay.barangay.lupon.model.PangkatCFA;
import com.barangay.barangay.lupon.model.PangkatComposition;
import com.barangay.barangay.lupon.repository.CaseRepository;
import com.barangay.barangay.lupon.repository.PangkatCFARepository;
import com.barangay.barangay.lupon.repository.PangkatCompositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PangkatCFAService {

    private final PangkatCFARepository pangkatCFARepository;
    private final CaseRepository caseRepository;
    private final AuditLogService auditLogService;
    private final PangkatCompositionRepository pangkatCompositionRepository;

        @Transactional
        public void issueCfa(CFARequest request , User user,String ipAddress) {
    
            BlotterCase blotter = caseRepository.findByBlotterNumber(request.blotterNumber())
                    .orElseThrow(() -> new RuntimeException("Blotter Case not found!"));
    
            if (pangkatCFARepository.findByBlotterCase_BlotterNumber(request.blotterNumber()).isPresent()) {
                throw new RuntimeException("CFA has already been issued for this case.");
            }
    
            PangkatCFA cfa = new  PangkatCFA();
    
            cfa.setBlotterCase(blotter);
            cfa.setGrounds(request.grounds());
            cfa.setIssuedBy(user);
            cfa.setSubjectOfLitigation(request.matterFiled());
            cfa.setControlNumber("CFA:" + blotter.getBlotterNumber());
    
            pangkatCFARepository.save(cfa);
    
            blotter.setStatus(CaseStatus.CERTIFIED_TO_FILE_ACTION);
            blotter.setIsCertified(true);
            blotter.setCertifiedAt(LocalDateTime.now());
            caseRepository.save(blotter);
    
    
            auditLogService.log(
                    user,
                    Departments.LUPONG_TAGAPAMAYAPA,
                    "ISSUED_CFA",
                    Severity.INFO,
                    "CFA officially issued for case number " + blotter.getBlotterNumber(),
                    ipAddress,
                    "Grounds: " + request.grounds(),
                    null,
                    null
            );


        }

    public CFAResponse getCfaDetails(String blotterNumber) {
        // 1. Fetch CFA record
        PangkatCFA cfa = pangkatCFARepository.findByBlotterCase_BlotterNumber(blotterNumber)
                .orElseThrow(() -> new RuntimeException("CFA record not found for case: " + blotterNumber));

        var blotter = cfa.getBlotterCase();
        var comp = blotter.getComplainant();
        var resp = blotter.getRespondent();

        List<PangkatComposition> composition = pangkatCompositionRepository.findByBlotterCase_BlotterNumber(blotterNumber);

        String luponChairman = "", chairmanPos = "";
        String luponSecretary = "", secretaryPos = "";
        String luponMember = "", memberPos = "";

        for (PangkatComposition p : composition) {
            String fullName = p.getEmployee().getPerson().getFirstName() + " " + p.getEmployee().getPerson().getLastName();
            String pos = p.getPosition().toLowerCase();

            if (pos.contains("chairman") || pos.contains("pangulo")) {
                luponChairman = fullName;
                chairmanPos = p.getPosition();
            } else if (pos.contains("secretary") || pos.contains("kalihim")) {
                luponSecretary = fullName;
                secretaryPos = p.getPosition();
            }
            else {
                luponMember = fullName;
                memberPos = p.getPosition();
            }
        }

        return new CFAResponse(
                blotter.getBlotterNumber(),
                cfa.getSubjectOfLitigation(),
                comp.getPerson().getFirstName() + " " + comp.getPerson().getLastName(),
                comp.getPerson().getCompleteAddress(),
                resp.getPerson().getFirstName() + " " + resp.getPerson().getLastName(),
                resp.getPerson().getCompleteAddress(),
                cfa.getGrounds(),
                cfa.getControlNumber(),
                cfa.getIssuedAt(),
                // Pangkat Data
                luponChairman,
                chairmanPos,
                luponSecretary,
                secretaryPos,
                luponMember,
                memberPos
        );
    }




}
