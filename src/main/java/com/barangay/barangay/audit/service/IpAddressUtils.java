package com.barangay.barangay.audit.service;

import jakarta.servlet.http.HttpServletRequest;

public class IpAddressUtils {

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }

        // Listahan ng mga common headers na pwedeng paglagyan ng client IP
        String[] headersToCheck = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headersToCheck) {
            String ip = request.getHeader(header);

            // I-check kung may laman ang header at hindi "unknown"
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Kung dumaan sa multiple proxies, ang first IP sa list ang tunay na client
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // Fallback kung walang proxy headers (e.g., direct connection o local testing)
        return request.getRemoteAddr();
    }
}