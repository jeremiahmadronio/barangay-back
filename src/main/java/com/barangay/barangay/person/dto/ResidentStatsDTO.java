package com.barangay.barangay.person.dto;

public record ResidentStatsDTO (
        Long totalResidents,
        Long totalVoters,
        Long totalSeniorCitizen,
        Long headsOfTheFamily
){
}
