package com.barangay.barangay.ftjs.dto;

import java.time.LocalDateTime;

public record NotesResponseDTO (
        Long id,
        String note,
        String createdBy,
        LocalDateTime createdAt
){
}
