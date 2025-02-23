package com.example.earthtalk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JwtCustomException extends RuntimeException{

    private final ErrorCode errorCode;
}
