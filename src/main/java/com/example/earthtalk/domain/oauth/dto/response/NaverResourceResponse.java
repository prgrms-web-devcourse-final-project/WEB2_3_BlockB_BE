package com.example.earthtalk.domain.oauth.dto.response;

import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.OAuth2AuthenticationException;
import java.util.Map;

public class NaverResourceResponse extends OAuth2UserResponse {

    public NaverResourceResponse(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes.get("response"));
    }

    @Override
    public String getId() {
        if (attributes == null) {
            throw new OAuth2AuthenticationException(ErrorCode.OAUTH_NOT_FOUND);
        }
        return (String) attributes.get("id");
    }

    @Override
    public String getNickname() {
        if (attributes == null) {
            throw new OAuth2AuthenticationException(ErrorCode.OAUTH_NOT_FOUND);
        }
        return (String) attributes.get("nickname");
    }

    @Override
    public String getImageUrl() {
        if (attributes == null) {
            throw new OAuth2AuthenticationException(ErrorCode.OAUTH_NOT_FOUND);
        }
        return (String) attributes.get("profile_image");
    }
}
