package com.layoof.layoof.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;

@Configuration
public class StorageConfig {

    private static final String R2_ENDPOINT = "https://%s.r2.cloudflarestorage.com";
    private static final String R2_REGION = "auto";

    private static final String LOCAL_RESOURCE_PATTERN = "/files/**";

    @Bean
    @ConditionalOnProperty(name = "layoof.storage.provider", havingValue = "r2")
    public S3Client r2Client(@Value("${layoof.storage.account-id:}") String accountId,
                             @Value("${layoof.storage.bucket:}") String bucket,
                             @Value("${layoof.storage.access-key:}") String accessKey,
                             @Value("${layoof.storage.secret-key:}") String secretKey) {

        requireR2Credentials(accountId, bucket, accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(R2_ENDPOINT.formatted(accountId)))
                .region(Region.of(R2_REGION))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "layoof.storage.provider", havingValue = "local", matchIfMissing = true)
    public WebMvcConfigurer localStorageResources(
            @Value("${layoof.storage.local-directory:uploads}") Path localDirectory) {

        String location = localDirectory.toAbsolutePath().normalize().toUri().toString();

        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler(LOCAL_RESOURCE_PATTERN).addResourceLocations(location);
            }
        };
    }

    private void requireR2Credentials(String... credentials) {
        if (Arrays.stream(credentials).anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalStateException(
                    "Configure R2_ACCOUNT_ID, R2_BUCKET, R2_ACCESS_KEY_ID e R2_SECRET_ACCESS_KEY "
                            + "para usar layoof.storage.provider=r2");
        }
    }
}
