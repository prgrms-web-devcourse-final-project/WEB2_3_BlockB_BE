package com.example.earthtalk.domain.oauth.dto.response;

import java.util.Map;

public class KakaoResourceResponse extends OAuth2UserResponse {

    public KakaoResourceResponse(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes.get("response"));
    }

    @Override
    public String getId() {
        if (attributes == null) {
            return null;
        }
        return (String) attributes.get("id");
    }

    @Override
    public String getNickname() {
        if (attributes == null) {
            return null;
        }

        return (String) attributes.get("nickname");
    }

    @Override
    public String getImageUrl() {
        if (attributes == null) {
            return null;
        }

        return (String) attributes.get("profile_image");
    }
}