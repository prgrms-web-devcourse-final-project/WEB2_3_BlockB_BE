package com.example.earthtalk.domain.oauth.service;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.oauth.dto.OAuthAttributes;
import com.example.earthtalk.domain.user.dto.request.UserInfoRequest;
import com.example.earthtalk.domain.user.entity.Role;
import com.example.earthtalk.domain.user.entity.SocialType;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.example.earthtalk.global.exception.IllegalArgumentException;
import com.example.earthtalk.global.security.dto.TokenResponse;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String NAVER = "naver";
    private static final String KAKAO = "kakao";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.loadUser() 실행 - OAuth2 로그인 요청 진입");

        // DefaultOAuth2UserService 객체를 활용해 유저 정보를 담는 OAuth2User 생성
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 소셜로그인 유저 정보 호출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialType socialType = getSocialType(registrationId);
        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint()
            .getUserNameAttributeName(); // OAuth2 로그인 시 키(PK)가 되는 값
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 소셜 로그인에서 API가 제공하는 유저 정보의 Json 값

        // SocialType에 따라 유저 정보를 통해 OAuthAttributes 객체 생성
        OAuthAttributes extractAttributes = OAuthAttributes.of(socialType, userNameAttributeName,
            attributes);

        User createdUser = getUser(extractAttributes, socialType); // getUser() 메소드로 User 객체 생성 후 반환

        // DefaultOAuth2User를 구현한 CustomOAuth2User 객체를 생성해서 반환
        return new CustomOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(createdUser.getRole().toString())),
            attributes,
            extractAttributes.getNameAttributeKey(),
            createdUser.getEmail(),
            createdUser.getRole()
        );
    }

    // AccessToken 만료시, AccessToken, RefreshToken 재발급
    public TokenResponse.GetToken getReissue(String bearerToken) {
        // TODO: Refresh Token 만료기간 관리 -> Redis 관리
        String refreshToken = jwtTokenProvider.parseBearerToken(bearerToken);
        Claims claims = jwtTokenProvider.validateRefreshToken(refreshToken);

        userRepository.findByEmail(claims.getSubject())
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        CustomOAuth2User customOAuth2User = jwtTokenProvider.getCustomOAuth2User(claims);

        return jwtTokenProvider.generateAllTokens(customOAuth2User, new Date());
    }

    // oauth 회원가입 완료후, 역할 변경
    public void completeSignup(UserInfoRequest userInfoRequest, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.ROLE_GUEST)) {
            throw new IllegalArgumentException(ErrorCode.EXIST_USER);
        }

        if (userRepository.existsByNickname(userInfoRequest.nickname())) {
            throw new IllegalArgumentException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateNickname(userInfoRequest.nickname());
        user.updateRole(Role.ROLE_MEMBER);
        userRepository.save(user);
    }

    // 소셜로그인 타입 반환
    private SocialType getSocialType(String registrationId) {
        if (NAVER.equals(registrationId)) {
            return SocialType.NAVER;
        }
        if (KAKAO.equals(registrationId)) {
            return SocialType.KAKAO;
        }
        return SocialType.GOOGLE;
    }

    /**
     * SocialType과 attributes에 들어있는 소셜 로그인의 식별값 id를 통해 회원을 찾아 반환하는 메소드<br> 만약 찾은 회원이 있다면, 그대로 반환하고
     * 없다면 saveUser()를 호출하여 회원을 저장한다.
     */
    private User getUser(OAuthAttributes attributes, SocialType socialType) {
        User findUser = userRepository.findBySocialTypeAndSocialId(socialType,
            attributes.getOauth2UserResponse().getId()).orElse(null);

        if (findUser == null) {
            return saveUser(attributes, socialType);
        }
        return findUser;
    }

    // 닉네임 입력을 안 받았기 때문에 임시 닉네임 설정 후, GUEST User 객체 생성 후 반환
    private User saveUser(OAuthAttributes attributes, SocialType socialType) {
        User createdUser = attributes.toEntity(socialType, attributes.getOauth2UserResponse());
        return userRepository.save(createdUser);
    }

}
