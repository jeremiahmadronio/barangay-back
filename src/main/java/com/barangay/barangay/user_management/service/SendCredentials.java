package com.barangay.barangay.user_management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendCredentials {

    private final JavaMailSender mailSender;

    public void sendCredentials(String toEmail, String fullName, String rawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nermamadronio@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Barangay Ugong — Your Staff Account Has Been Created");

        String body = String.format("""
                Good day, %s,

                Your staff account for the Barangay Ugong Management System has been successfully created by the System Administrator.

                Please use the following credentials to access the system:

                    Email Address : %s
                    Temporary Password : %s

                For your security, you will be prompted to change your password upon your first login. Please do not share your credentials with anyone.

                If you believe you received this email in error or did not expect this message, please contact the System Administrator immediately.

                This is a system-generated email. Please do not reply to this message.

                Respectfully,
                Barangay Ugong, Valenzuela City
                Barangay Management System
                """,
                fullName, toEmail, rawPassword
        );

        message.setText(body);
        mailSender.send(message);
    }
}