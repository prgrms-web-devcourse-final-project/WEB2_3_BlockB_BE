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
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.example.earthtalk.global.exception.IllegalArgumentException;
import com.example.earthtalk.global.exception.OAuth2AuthenticationException;
import com.example.earthtalk.domain.oauth.dto.response.TokenResponse;
import com.example.earthtalk.global.exception.UserLockedException;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Transactional
    public TokenResponse.GetOauth getResource(String provider, String authCode) {
        if (authCode == null || authCode.isEmpty()) {
            throw new OAuth2AuthenticationException(ErrorCode.NOTFOUND_OAUTH_TOKEN);
        }
        ClientRegistration clientRegistration = getClientRegistration(provider);
        OAuthClient oAuthClient = oauthClients.get(provider);

        log.info("call OAuthService.getIdTokenFromAuthCode[authCode: {}]", authCode);
        TokenResponse.GetToken tokens = oAuthClient.getAccessTokenFromAuthCode(clientRegistration,
            authCode);
        return getResourceFromToken(oAuthClient, tokens, clientRegistration);
    }

    public TokenResponse.GetOauth getResourceFromToken(OAuthClient oAuthClient, TokenResponse.GetToken tokens,
        ClientRegistration clientRegistration) {

        // 소셜 타입 결정
        SocialType socialType = getSocialType(clientRegistration.getClientName());

        // userAttributes에서 필요한 정보 추출
        String userNameAttributeName = clientRegistration
            .getProviderDetails().getUserInfoEndpoint()
            .getUserNameAttributeName();

        // Access Token으로 사용자 정보 가져오기
        Map<String, Object> userAttributes = oAuthClient.getProfileFromAccessToken(
            clientRegistration, tokens.accessToken());
        log.info("userAttributes: {}", userAttributes);

        OAuthAttributes extractAttributes = OAuthAttributes.of(
            socialType,
            userNameAttributeName,
            userAttributes
        );

        User user = getOrSaveUser(extractAttributes, socialType, tokens); // 유저 정보 저장 또는 업데이트
        CustomOAuth2User oAuth2User = setAuthenticationContext(user, userAttributes,
            extractAttributes.getNameAttributeKey()); // 인증 객체 생성 및 SecurityContext에 설정

        TokenResponse.GetToken tokenResponse = jwtTokenProvider.generateAllTokens(oAuth2User,
            new Date());
        saveRefreshToken(oAuth2User, tokenResponse.refreshToken());

        // 프론트엔드로 JWT, 프로필 포함하여 반환
        // TODO: token은 cookie로 변경, 프로필 정보는 API 따로 호출 고려
        return TokenResponse.GetOauth.from(
            user.getId(),
            tokenResponse.accessToken(),
            tokenResponse.refreshToken(),
            user.getNickname(),
            user.getProfileUrl(),
            user.getRole().toString());
    }

    // AccessToken 만료시, AccessToken, RefreshToken 재발급
    @Transactional
    public TokenResponse.GetToken getReissue(String refreshToken) {
        // TODO: Refresh Token 만료기간 관리 -> Redis 관리
        RefreshToken originToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new NotFoundException(ErrorCode.INVALID_REFRESH_TOKEN));

        Claims claims = jwtTokenProvider.validateRefreshToken(refreshToken);

        userRepository.findByEmail(claims.getSubject())
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        CustomOAuth2User customOAuth2User = jwtTokenProvider.getCustomOAuth2User(claims);
        TokenResponse.GetToken newTokens = jwtTokenProvider.generateAllTokens(customOAuth2User,
            new Date());
        originToken.updateToken(newTokens.refreshToken());

        return newTokens;
    }

    public void confirmNickname(UserInfoRequest.ConfirmNickname userInfoRequest) {
        if (userRepository.existsByNickname(userInfoRequest.nickname())) {
            throw new IllegalArgumentException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // oauth 회원가입 완료후, 역할 변경
    @Transactional
    public void completeSignup(UserInfoRequest.Signup userInfoRequest, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.ROLE_GUEST)) {
            throw new IllegalArgumentException(ErrorCode.EXIST_USER);
        }

        user.updateSignupInfo(userInfoRequest.nickname(), userInfoRequest.introduction());
        user.updateRole(Role.ROLE_MEMBER);
    }

    //TODO: save 삭제 후 transactional 처리
    public void saveRefreshToken(CustomOAuth2User oAuth2User, String refreshToken) {
        String userEmail = oAuth2User.getEmail();

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserEmail(userEmail)
            .orElse(RefreshToken.builder()
                .userEmail(userEmail)
                .build());

        refreshTokenEntity.updateToken(refreshToken); // 새로운 토큰 값 설정
        refreshTokenRepository.save(refreshTokenEntity);
    }

    /**
     * SocialType과 attributes에 들어있는 소셜 로그인의 식별값 id를 통해 회원을 찾아 반환하는 메소드<br> 만약 찾은 회원이 있다면, 그대로 반환하고
     * 없다면 saveUser()를 호출하여 회원을 저장한다.
     */
    public User getOrSaveUser(OAuthAttributes attributes, SocialType socialType,
        TokenResponse.GetToken tokens) {
        User findUser = userRepository.findBySocialTypeAndSocialId(socialType,
            attributes.getOauth2UserResponse().getId()).orElse(null);

        if (findUser == null) {
            return saveUser(attributes, socialType, tokens);
        }
        // 신고 예외 처리
        validateUserStatus(findUser);
        findUser.updateLoginInfo(attributes.getOauth2UserResponse().getImageUrl(),
            tokens.accessToken(), tokens.refreshToken());
        return userRepository.save(findUser);
    }

    private ClientRegistration getClientRegistration(String provider) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);
        if (clientRegistration == null) {
            throw new IllegalArgumentException(ErrorCode.INVALID_SOCIAL_TYPE);
        }
        return clientRegistration;
    }

    private CustomOAuth2User setAuthenticationContext(User user, Map<String, Object> attributes,
        String nameAttributeKey) {
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

    // 닉네임 입력을 안 받았기 때문에 임시 닉네임 설정 후, GUEST User 객체 생성 후 반환
    private User saveUser(OAuthAttributes attributes, SocialType socialType,
        TokenResponse.GetToken tokens) {
        User createdUser = attributes.toEntity(socialType, attributes.getOauth2UserResponse(), tokens);
        return userRepository.save(createdUser);
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

    private void validateUserStatus(User user) {
        if (user.isSuspended()) {
            // 3일이 지나지 않아서 계정 정지 상태
            if (!user.isSuspensionPeriodOver()) {
                long remainingDays = ChronoUnit.DAYS.between(
                    LocalDateTime.now(), user.getSuspendedAt().plusDays(3)
                );
                throw new UserLockedException(ErrorCode.REPORT_SUSPENDED_USER,
                    String.format(ErrorCode.REPORT_SUSPENDED_USER.getMessage(), remainingDays)
                );
            }
            // 3일이 지나면 상태 복구
            user.restoreUser();
        }
        if (user.isBanned()) {
            throw new BadRequestException(ErrorCode.REPORT_BANNED_USER);
        }
    }
}
