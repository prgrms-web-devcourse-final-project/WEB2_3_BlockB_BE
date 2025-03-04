package com.example.earthtalk.domain.oauth.dto.response;

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
        Long userId,
        String accessToken,
        String refreshToken,
        String nickname,
        String imgUrl,
        String role
    ) {
        public static GetOauth from(final Long userId, final String accessToken, final String refreshToken, final String nickname, final String imgUrl, final String role) {
            return new GetOauth(userId, accessToken, refreshToken, nickname, imgUrl, role);
        }
    }
}
