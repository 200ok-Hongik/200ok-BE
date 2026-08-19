package com.team202ok.demo.domain.ai.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class FakeImageUploader implements ImageUploader {

    @Override
    public String upload(MultipartFile file) {
        // MVP: 실제 업로드 없이 캘린더 UI에서 바로 보이는 더미 이미지 URL 반환
        // 나중에 S3Uploader로 교체 예정
        return "https://picsum.photos/seed/" + UUID.randomUUID() + "/400/400";
    }
}
