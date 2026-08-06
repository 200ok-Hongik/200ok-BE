package com.team202ok.demo.global.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String categoryCode) {
        super("존재하지 않는 품목 코드입니다: " + categoryCode);
    }
}
