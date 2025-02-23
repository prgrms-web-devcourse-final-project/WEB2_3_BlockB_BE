package com.example.earthtalk.domain.oauth.handler;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.oauth.entity.RefreshToken;
import com.example.earthtalk.domain.oauth.repository.RefreshTokenRepository;
import com.example.earthtalk.global.response.ApiResponse;
import com.example.earthtalk.global.security.dto.TokenResponse;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Date date = new Date();
        TokenResponse tokenResponse = jwtTokenProvider.generateAllTokens(oAuth2User, date);

        saveRefreshToken(oAuth2User, tokenResponse.refreshToken());

        // JSON 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");

        // 응답 본문에 토큰 정보 작성
        String responseBody = objectMapper.writeValueAsString(
            ApiResponse.createSuccess(tokenResponse));
        response.getWriter().write(responseBody);
    }

    private void saveRefreshToken(CustomOAuth2User oAuth2User, String refreshToken) {
        String userEmail = oAuth2User.getEmail();

        // 기존 Refresh Token 삭제
        if (refreshTokenRepository.existsByUserEmail(userEmail)) {
            refreshTokenRepository.deleteByUserEmail(userEmail);
        }

        // 리프레시 토큰 DB에 저장
        refreshTokenRepository.save(
            RefreshToken.builder()
            .userEmail(oAuth2User.getEmail())
            .token(refreshToken)
            .build()
        );
    }
}
