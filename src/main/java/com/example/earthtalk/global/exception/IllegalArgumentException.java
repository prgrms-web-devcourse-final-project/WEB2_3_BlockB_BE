package com.example.earthtalk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class IllegalArgumentException extends RuntimeException {
  private final ErrorCode errorCode;
}
