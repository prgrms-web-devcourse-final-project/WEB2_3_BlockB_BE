package com.example.earthtalk.domain.oauth.dto.response.accessToken;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class KakaoAccessTokenResponse {

    @JsonProperty("access_token")
    private String accessToken; // 구글 액세스 토큰

    @JsonProperty("refresh_token")
    private String refreshToken; // 구글 액세스 토큰

    @JsonProperty("expires_in")
    private String expiresIn; // 구글 액세스 토큰 만료 시간 (초 단위)

    @JsonProperty("refresh_token_expires_in")
    private String refreshTokenExpiresIn;

    @JsonProperty("token_type")
    private String tokenType; // 토큰 유형
}
