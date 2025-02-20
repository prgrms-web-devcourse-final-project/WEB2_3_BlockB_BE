package com.example.earthtalk.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ResponseStatus(HttpStatus.CONFLICT)
@RequiredArgsConstructor
@Getter
public class ConflictException extends RuntimeException {

	private final ErrorCode errorCode;
}
