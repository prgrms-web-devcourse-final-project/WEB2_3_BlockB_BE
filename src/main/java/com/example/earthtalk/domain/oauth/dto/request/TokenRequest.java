package com.example.earthtalk.domain.oauth.dto.request;


public record TokenRequest(
    String refreshToken
) {
    public static TokenRequest from(final String refreshToken) {
        return new TokenRequest(refreshToken);
    }
}
