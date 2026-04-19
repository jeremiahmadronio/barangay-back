package com.barangay.barangay.lupon.dto.CFA;

import java.time.LocalDateTime;

public record CFAResponse (

         String blotterNumber,
         String matterFiled,
         String complinantName,
         String complinantAddress,
         String respondentName,
         String respondentAddress,
         String grounds,
         String controlNumber,
         LocalDateTime issuedAt,

String luponChairman,
         String chairmanPosition,
         String luponSecretary,
         String secretaryPosition,
         String luponMember,
         String memberPosition




) {
}
