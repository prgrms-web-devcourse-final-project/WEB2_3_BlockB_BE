package com.example.earthtalk.domain.oauth.dto;

import com.example.earthtalk.domain.oauth.dto.response.GoogleResourceResponse;
import com.example.earthtalk.domain.oauth.dto.response.KakaoResourceResponse;
import com.example.earthtalk.domain.oauth.dto.response.NaverResourceResponse;
import com.example.earthtalk.domain.oauth.dto.response.OAuth2UserResponse;
import com.example.earthtalk.domain.user.entity.Role;
import com.example.earthtalk.domain.user.entity.SocialType;
import com.example.earthtalk.domain.user.entity.User;
import java.util.Map;
import java.util.UUID;
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
     * SocialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환
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

    /**
     * OAuth2 정보를 추가하여 User 객체 생성후 DB 저장
     *
     * @param socialType
     * @param oauth2UserResponse
     * @return User
     */
    public User toEntity(SocialType socialType, OAuth2UserResponse oauth2UserResponse) {
        String id = oauth2UserResponse.getId();

        return User.builder()
            .email(UUID.randomUUID() + "@socialUser.com") // 식별을 위한 랜덤한 고유값 부여
            .socialType(socialType)
            .socialId(oauth2UserResponse.getId()) // 소셜로그인 제공받은 ID
            .nickname(id + "_" + oauth2UserResponse.getNickname()) // 소셜 닉네임
            .profileUrl(oauth2UserResponse.getImageUrl()) // 프로필 이미지
            .role(Role.ROLE_GUEST)
            .build();
    }
}
