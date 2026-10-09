package com.team202ok.demo.domain.ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class S3ImageUploaderTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final S3ImageUploader uploader = new S3ImageUploader(
            s3Client, "scan-bucket", "https://images.example.com/");

    @Test
    void uploadsSupportedImageAndReturnsPublicUrl() {
        MockMultipartFile image = new MockMultipartFile(
                "image", "scan.png", "image/png", new byte[]{1, 2, 3});

        String url = uploader.upload(image);

        assertThat(url).startsWith("https://images.example.com/scans/").endsWith(".png");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void rejectsUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "scan.svg", "image/svg+xml", "<svg/>".getBytes());

        assertThatThrownBy(() -> uploader.upload(file))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
