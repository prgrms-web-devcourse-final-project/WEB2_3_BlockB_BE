package com.example.earthtalk.domain.oauth.service;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.oauth.entity.RefreshToken;
import com.example.earthtalk.domain.oauth.repository.RefreshTokenRepository;
import com.example.earthtalk.domain.oauth.dto.OAuthAttributes;
import com.example.earthtalk.domain.oauth.util.OAuthClient;
import com.example.earthtalk.domain.user.dto.request.UserInfoRequest;
import com.example.earthtalk.domain.user.entity.Role;
import com.example.earthtalk.domain.user.entity.SocialType;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.example.earthtalk.global.exception.OAuth2AuthenticationException;
import com.example.earthtalk.domain.oauth.dto.response.TokenResponse;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, OAuthClient> oauthClients;
    private static final String NAVER = "naver";
    private static final String KAKAO = "kakao";

    public TokenResponse.GetOauth getResource(HttpServletRequest request, HttpServletResponse response, String provider, String authCode) {
        if (authCode == null || authCode.isEmpty()) {
            throw new OAuth2AuthenticationException(ErrorCode.NOTFOUND_OAUTH_TOKEN);
        }
        ClientRegistration clientRegistration = getClientRegistration(provider);
        OAuthClient oAuthClient = oauthClients.get(provider);

        log.info("call OAuthService.getIdTokenFromAuthCode[authCode: {}]", authCode);
        String accessToken = oAuthClient.getAccessTokenFromAuthCode(clientRegistration, authCode);
        return getResourceFromToken(request, response, oAuthClient, accessToken, clientRegistration);
    }

    public TokenResponse.GetOauth getResourceFromToken(HttpServletRequest request, HttpServletResponse response,
        OAuthClient oAuthClient, String accessToken, ClientRegistration clientRegistration) {

        // 소셜 타입 결정
        SocialType socialType = getSocialType(clientRegistration.getClientName());

        // userAttributes에서 필요한 정보 추출
        String userNameAttributeName = clientRegistration
            .getProviderDetails().getUserInfoEndpoint()
            .getUserNameAttributeName();

        // Access Token으로 사용자 정보 가져오기
        Map<String, Object> userAttributes = oAuthClient.getProfileFromAccessToken(clientRegistration, accessToken);
        log.info("userAttributes: {}", userAttributes);

        OAuthAttributes extractAttributes = OAuthAttributes.of(
            socialType,
            userNameAttributeName,
            userAttributes
        );

        User user = getOrSaveUser(extractAttributes, socialType); // 유저 정보 저장 또는 업데이트
        CustomOAuth2User oAuth2User = setAuthenticationContext(user, userAttributes, extractAttributes.getNameAttributeKey()); // 인증 객체 생성 및 SecurityContext에 설정

        TokenResponse.GetToken tokenResponse = jwtTokenProvider.generateAllTokens(oAuth2User, new Date());
        saveRefreshToken(oAuth2User, tokenResponse.refreshToken());

        String userNickname = user.getNickname();
        if (user.getRole().equals(Role.ROLE_GUEST)) {
            userNickname = user.getNickname().split("_")[1];
        }

        // 프론트엔드로 JWT, 닉네임, 프로필 포함하여 반환
        // TODO: token은 cookie로 변경, 프로필 정보는 API 따로 호출 고려
        return TokenResponse.GetOauth.from(
            user.getId(),
            tokenResponse.accessToken(),
            tokenResponse.refreshToken(),
            userNickname, //nickname split해서 주기
            user.getProfileUrl(),
            user.getRole().toString());
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
            throw new com.example.earthtalk.global.exception.IllegalArgumentException(ErrorCode.EXIST_USER);
        }

        if (userRepository.existsByNickname(userInfoRequest.nickname())) {
            throw new com.example.earthtalk.global.exception.IllegalArgumentException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateNickname(userInfoRequest.nickname());
        user.updateRole(Role.ROLE_MEMBER);
        userRepository.save(user);
    }

    private ClientRegistration getClientRegistration(String provider) {
        ClientRegistration clientRegistration = ((InMemoryClientRegistrationRepository) clientRegistrationRepository).findByRegistrationId(provider);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("지원되지 않는 OAuth 타입입니다.");
        }
        return clientRegistration;
    }

    private CustomOAuth2User setAuthenticationContext(User user, Map<String, Object> attributes, String nameAttributeKey) {
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(user.getRole().toString())),
            attributes,
            nameAttributeKey,
            user.getEmail(),
            user.getRole()
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                customOAuth2User.getAuthorities()
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return customOAuth2User;
    }

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
    private User getOrSaveUser(OAuthAttributes attributes, SocialType socialType) {
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

    private void saveRefreshToken(CustomOAuth2User oAuth2User, String refreshToken) {
        String userEmail = oAuth2User.getEmail();

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserEmail(userEmail)
            .orElse(RefreshToken.builder()
                .userEmail(userEmail)
                .build());

        refreshTokenEntity.updateToken(refreshToken); // 새로운 토큰 값 설정
        refreshTokenRepository.save(refreshTokenEntity); // 토큰 존재하면 수정, 없으면 저장
    }
}
