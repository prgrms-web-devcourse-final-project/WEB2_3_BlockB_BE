package com.example.earthtalk.global.security.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {
    public static TokenResponse from(final String accessToken, final String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}
