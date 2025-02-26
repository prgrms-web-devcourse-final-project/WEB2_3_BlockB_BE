package com.example.earthtalk.domain.oauth.handler;

import com.example.earthtalk.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173/oauth/callback/error";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("소셜 로그인에 실패했습니다. 에러 메시지 : {} | {}", exception.getMessage(),exception.getStackTrace());
        log.error("에러 경로 : {}", (Object) exception.getStackTrace());

        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = DEFAULT_REDIRECT_URI; // 기본 URL
        }

        // 로그인 실패 사유를 쿼리 파라미터에 포함하여 프론트엔드로 리디렉트
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("error", URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8))
            .queryParam("error_code", ErrorCode.OAUTH_LOGIN_FALIED.getCode())
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}