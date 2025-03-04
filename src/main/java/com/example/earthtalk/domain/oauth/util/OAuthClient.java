package com.example.earthtalk.domain.oauth.util;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import java.util.Map;

public interface OAuthClient {
    String getAccessTokenFromAuthCode(ClientRegistration clientRegistration, String authCode);
    Map<String, Object> getProfileFromAccessToken(ClientRegistration clientRegistration, String accessToken);
}

