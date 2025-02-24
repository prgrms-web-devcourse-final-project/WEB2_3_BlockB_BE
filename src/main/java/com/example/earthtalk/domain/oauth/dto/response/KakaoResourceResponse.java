package com.example.earthtalk.domain.oauth.dto.response;

import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.OAuth2AuthenticationException;
import java.util.Map;

public class KakaoResourceResponse extends OAuth2UserResponse {

    public KakaoResourceResponse(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getNickname() {
        Map<String, Object> profile = getOAuthInformation();
        return (String) profile.get("nickname");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> profile = getOAuthInformation();
        return (String) profile.get("thumbnail_image_url");
    }

    private Map<String, Object> getOAuthInformation() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        if (account == null) {
            throw new OAuth2AuthenticationException(ErrorCode.OAUTH_NOT_FOUND);
        }

        Map<String, Object> profile = (Map<String, Object>) account.get("profile");
        if (profile == null) {
            throw new OAuth2AuthenticationException(ErrorCode.KAKAO_PROFILE_NOT_FOUND);
        }

        return profile;
    }
}