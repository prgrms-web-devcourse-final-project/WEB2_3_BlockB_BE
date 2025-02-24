package com.example.earthtalk.domain.oauth.handler;

import com.example.earthtalk.global.response.ApiResponse;
import com.example.earthtalk.global.security.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("소셜 로그인에 실패했습니다. 에러 메시지 : {} | {}", exception.getMessage(),exception.getStackTrace());
        log.error("에러 경로 : {}", (Object) exception.getStackTrace());

        // 오류 메세지 반환
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseBody = objectMapper.writeValueAsString(
            ApiResponse.createError("[Sociallogin Error]: login failed"));
        response.getWriter().write(responseBody);
    }
}