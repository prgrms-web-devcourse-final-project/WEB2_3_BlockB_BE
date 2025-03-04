package com.example.earthtalk.domain.oauth.dto.request;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public record SocialAccessTokenRequest(String code, String clientId, String clientSecret,
                                       String redirectUri) {

    public MultiValueMap<String, String> toFormData() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        return body;
    }
}
