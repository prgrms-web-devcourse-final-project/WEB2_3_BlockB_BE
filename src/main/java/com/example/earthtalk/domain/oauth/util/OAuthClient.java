package com.example.earthtalk.domain.oauth.util;

import com.example.earthtalk.domain.oauth.dto.response.TokenResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import java.util.Map;

public interface OAuthClient {
    TokenResponse.GetToken getAccessTokenFromAuthCode(ClientRegistration clientRegistration, String authCode);
    Map<String, Object> getProfileFromAccessToken(ClientRegistration clientRegistration, String accessToken);
}

