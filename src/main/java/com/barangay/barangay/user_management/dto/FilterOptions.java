package com.barangay.barangay.user_management.dto;

import java.util.List;

public record FilterOptions(List<String> roles, List<String> departments) {}