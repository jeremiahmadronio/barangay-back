package com.barangay.barangay.resident.dto;

public record ResidentStatsDTO (
        Long totalResidents,
        Long totalVoters,
        Long totalSeniorCitizen,
        Long headsOfTheFamily
){
}
