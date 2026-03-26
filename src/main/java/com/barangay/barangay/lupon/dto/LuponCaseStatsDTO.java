package com.barangay.barangay.lupon.dto;

public record LuponCaseStatsDTO (
        long totalReferred,
        long activeConciliation,
        long successfullySettled,
        long cfaIssued
){
}
