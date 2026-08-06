package com.team202ok.demo.domain.analysis.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {
    String upload(MultipartFile file);
}