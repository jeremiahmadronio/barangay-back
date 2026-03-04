package com.barangay.barangay.auth.dto;

import java.util.UUID;

public record LoginResponse(

    String token,
    UUID userId,
    String role
            ){
}
