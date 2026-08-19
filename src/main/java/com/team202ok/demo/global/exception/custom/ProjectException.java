package com.team202ok.demo.global.exception.custom;

import com.team202ok.demo.global.exception.code.BaseErrorCode;

import lombok.Getter;

@Getter
public class ProjectException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public ProjectException(BaseErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ProjectException(BaseErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
