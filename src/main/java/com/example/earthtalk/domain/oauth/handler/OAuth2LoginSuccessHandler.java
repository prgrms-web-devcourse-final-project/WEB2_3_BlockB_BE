package com.example.earthtalk.domain.oauth.handler;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.oauth.entity.RefreshToken;
import com.example.earthtalk.domain.oauth.repository.RefreshTokenRepository;
import com.example.earthtalk.domain.user.entity.Role;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.example.earthtalk.global.security.dto.TokenResponse;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private static final String DEFAULT_SIGNUP_REDIRECT_URI = "http://localhost:5173/oauth/callback";
    private static final String DEFAULT_LOGIN_REDIRECT_URI = "http://localhost:5173/oauth/loginsuccess";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        log.info("OAuth2 Login 성공!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Date date = new Date();
        TokenResponse.GetToken tokenResponse = jwtTokenProvider.generateAllTokens(oAuth2User, date);

        User user = userRepository.findByEmail(oAuth2User.getEmail())
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        saveRefreshToken(oAuth2User, tokenResponse.refreshToken());

        // JSON 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");

        String targetUrl;
        if (user.getRole().equals(Role.ROLE_GUEST)) {
            // 프론트엔드로 JWT, 닉네임, 프로필 포함하여 리디렉트
            // TODO: token은 cookie로 변경, 프로필 정보는 API 따로 호출 고려
            targetUrl = UriComponentsBuilder.fromUriString(DEFAULT_SIGNUP_REDIRECT_URI)
                .queryParam("access_token", tokenResponse.accessToken())
                .queryParam("refresh_token", tokenResponse.refreshToken())
                .queryParam("nickname", URLEncoder.encode(user.getNickname().split("_")[1], StandardCharsets.UTF_8))
                .queryParam("profileUrl", user.getProfileUrl())
                .build().toUriString();
        } else {
            targetUrl = UriComponentsBuilder.fromUriString(DEFAULT_LOGIN_REDIRECT_URI)
                .queryParam("access_token", tokenResponse.accessToken())
                .queryParam("refresh_token", tokenResponse.refreshToken())
                .build().toUriString();
        }

        // HTTP 302 Redirect 응답
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    public void saveRefreshToken(CustomOAuth2User oAuth2User, String refreshToken) {
        String userEmail = oAuth2User.getEmail();

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserEmail(userEmail)
            .orElse(RefreshToken.builder()
                .userEmail(userEmail)
                .build());

        refreshTokenEntity.updateToken(refreshToken); // 새로운 토큰 값 설정
        refreshTokenRepository.save(refreshTokenEntity); // 토큰 존재하면 수정, 없으면 저장
    }
}
