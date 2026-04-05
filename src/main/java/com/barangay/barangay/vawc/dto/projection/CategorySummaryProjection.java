package com.barangay.barangay.vawc.dto.projection;

public interface CategorySummaryProjection {

    String getCategory();
    Long getTotalCases();
    Long getActive();
    Long getResolved();
    Long getPending();
    Double getPercentage();
}

