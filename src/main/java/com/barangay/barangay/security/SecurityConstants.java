package com.barangay.barangay.security;

public class SecurityConstants {

    public static final String ROLE_ROOT_ADMIN = "ROOT_ADMIN";


    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/error"
    };

    public static final String[] ROOT_ADMIN_ENDPOINTS = {
            "/api/v1/users/create-admin",
            "/api/v1/dashboard/**",
            "/api/v1/audit/**"
    };

    public static final String[] ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "http://localhost:3000"
    };

    public static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};
    public static final String[] ALLOWED_HEADERS = {"*"};

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}