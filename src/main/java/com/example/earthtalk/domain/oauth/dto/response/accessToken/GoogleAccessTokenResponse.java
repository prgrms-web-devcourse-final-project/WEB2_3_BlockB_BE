package com.example.earthtalk.domain.oauth.dto.response.accessToken;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class GoogleAccessTokenResponse {

    @JsonProperty("access_token")
    private String accessToken; // 구글 액세스 토큰

    @JsonProperty("refresh_token")
    private String refreshToken; // 구글 액세스 토큰

    @JsonProperty("expires_in")
    private String expiresIn; // 구글 액세스 토큰 만료 시간 (초 단위)

    private String scope; // 조회하고자 하는 API 정보

    @JsonProperty("token_type")
    private String tokenType; // 토큰 유형

    @JsonProperty("id_token")
    private String idToken;
}
