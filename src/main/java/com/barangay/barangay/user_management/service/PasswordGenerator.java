package com.barangay.barangay.user_management.service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public class PasswordGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";

    public static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        return random.ints(length, 0, CHARS.length())
                .mapToObj(CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
