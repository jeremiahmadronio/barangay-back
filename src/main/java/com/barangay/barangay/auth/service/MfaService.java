package com.barangay.barangay.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final JavaMailSender mailSender;


    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    public void sendMfaEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nermamadronio@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Barangay Management System – Verification Code");

        message.setText(
                "Good day,\n\n" +
                        "You are receiving this email because a verification request was made for your account.\n\n" +
                        "Your 6-digit verification code is:\n\n" +
                        code + "\n\n" +
                        "This code will expire in 5 minutes. Please do not share this code with anyone.\n\n" +
                        "If you did not request this verification, please ignore this email.\n\n" +
                        "Thank you,\n" +
                        "Barangay Management System"
        );

        mailSender.send(message);
    }
}