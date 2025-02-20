package com.example.earthtalk.domain.oauth.dto;

import com.example.earthtalk.domain.oauth.dto.response.GoogleResourceResponse;
import com.example.earthtalk.domain.oauth.dto.response.KakaoResourceResponse;
import com.example.earthtalk.domain.oauth.dto.response.NaverResourceResponse;
import com.example.earthtalk.domain.oauth.dto.response.OAuth2UserResponse;
import com.example.earthtalk.domain.user.entity.SocialType;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * 각 소셜에서 받아오는 데이터가 다르므로
 * 소셜별로 데이터를 받는 데이터를 분기 처리하는 DTO 클래스
 */
@Getter
public class OAuthAttributes {

    private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
    private OAuth2UserResponse oauth2UserResponse; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등등)

    @Builder
    public OAuthAttributes(String nameAttributeKey, OAuth2UserResponse oauth2UserResponse) {
        this.nameAttributeKey = nameAttributeKey;
        this.oauth2UserResponse = oauth2UserResponse;
    }

    /**
     * SocialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환<br>
     * 소셜별 of 메소드(ofGoogle, ofKaKao, ofNaver)들은 각각 소셜 로그인 API에서 <br>
     * 회원의 식별값(id), attributes, nameAttributeKey를 저장 후 build
     *
     * @param socialType 소셜로그인 타입
     * @param userNameAttributeName OAuth2 로그인시 키(PK)가 되는 값
     * @param attributes OAuth 서비스의 유저 정보들
     */
    public static OAuthAttributes of(SocialType socialType,
        String userNameAttributeName, Map<String, Object> attributes) {

        if (socialType == SocialType.NAVER) {
            return ofNaver(userNameAttributeName, attributes);
        }
        if (socialType == SocialType.KAKAO) {
            return ofKakao(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
            .nameAttributeKey(userNameAttributeName)
            .oauth2UserResponse(new KakaoResourceResponse(attributes))
            .build();
    }

    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
            .nameAttributeKey(userNameAttributeName)
            .oauth2UserResponse(new GoogleResourceResponse(attributes))
            .build();
    }

    public static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
            .nameAttributeKey(userNameAttributeName)
            .oauth2UserResponse(new NaverResourceResponse(attributes))
            .build();
    }

}
