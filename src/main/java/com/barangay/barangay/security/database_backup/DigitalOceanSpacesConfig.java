package com.barangay.barangay.security.database_backup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class DigitalOceanSpacesConfig {

    @Value("${DO_SPACES_KEY}")
    private String accessKey;

    @Value("${DO_SPACES_SECRET}")
    private String secretKey;

    @Value("${DO_SPACES_ENDPOINT}")
    private String endpoint;

    @Value("${DO_SPACES_REGION}")
    private String region;

    @Bean
    public S3Client s3Client() {
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true) // Eto ang gamot sa SSL error mo
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .serviceConfiguration(serviceConfiguration) // <--- I-apply ang config dito
                .build();
    }
}