package com.example.earthtalk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SaveFailedException extends RuntimeException {
	private final ErrorCode errorCode;

}
