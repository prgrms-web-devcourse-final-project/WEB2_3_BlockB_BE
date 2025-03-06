package com.example.earthtalk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserLockedException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;
}
