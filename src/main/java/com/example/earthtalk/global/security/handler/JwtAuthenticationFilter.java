package com.example.earthtalk.global.security.handler;

import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.JwtCustomException;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {
            "/api-docs/**",
            "/swagger-ui/**",
            "/api/auth/reissue",
            "/login/**",
            "/oauth2/**",
            "/api/oauth2/**",
            "/earth_talk/**"
        };

        AntPathMatcher pathMatcher = new AntPathMatcher();
        String path = new UrlPathHelper().getPathWithinApplication(request);
        return Arrays.stream(excludePath)
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String token = jwtTokenProvider.parseBearerToken(request.getHeader(AUTHORIZATION_HEADER));
        log.info("시큐리티 필터링 실행");

        //토큰 정보 유효하면 securityContextHolder 에 사용자 인증 정보저장하고 다음 filter 진행
        //토큰 유효하지 않으면 JWTFilterExceptionHandler 에서 예외 처리
        if (StringUtils.hasText(token) && jwtTokenProvider.validateAccessToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } else {
            throw new JwtCustomException(ErrorCode.EMPTY_JWT_TOKEN);
        }
    }
}

