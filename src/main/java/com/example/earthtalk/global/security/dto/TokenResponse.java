package com.example.earthtalk.global.security.dto;

public record TokenResponse() {

    public record GetToken(
        String accessToken,
        String refreshToken
    ) {
        public static GetToken from(final String accessToken, final String refreshToken) {
            return new GetToken(accessToken, refreshToken);
        }
    }

    public record GetOauth(
        String accessToken,
        String refreshToken,
        String nickname,
        String imgUrl
    ) {
        public static GetOauth from(final String accessToken, final String refreshToken, final String nickname, final String imgUrl) {
            return new GetOauth(accessToken, refreshToken, nickname, imgUrl);
        }
    }
}
