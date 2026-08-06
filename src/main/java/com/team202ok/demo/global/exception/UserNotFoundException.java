package com.team202ok.demo.global.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("존재하지 않는 사용자입니다. userId=" + userId);
    }
}