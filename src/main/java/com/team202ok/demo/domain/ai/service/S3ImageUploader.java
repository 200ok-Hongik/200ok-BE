package com.team202ok.demo.domain.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "true")
public class S3ImageUploader implements ImageUploader {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public S3ImageUploader(S3Client s3Client,
                           @Value("${storage.s3.bucket}") String bucket,
                           @Value("${storage.s3.public-base-url}") String publicBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = stripTrailingSlash(publicBaseUrl);
    }

    @Override
    public String upload(MultipartFile file) {
        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        if (file.isEmpty() || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPEG, PNG 또는 WebP 이미지가 필요합니다.");
        }

        String key = createKey(contentType);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new IllegalStateException("이미지를 읽을 수 없습니다.", e);
        }
        return URI.create(publicBaseUrl + "/" + key).toString();
    }

    private String createKey(String contentType) {
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return "scans/%d/%02d/%02d/%s%s".formatted(
                today.getYear(), today.getMonthValue(), today.getDayOfMonth(), UUID.randomUUID(), extension);
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
