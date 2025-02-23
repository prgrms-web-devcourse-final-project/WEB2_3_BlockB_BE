package com.example.earthtalk.global.security.util;

import static com.example.earthtalk.global.exception.ErrorCode.EXPIRED_ACCESS_TOKEN;
import static com.example.earthtalk.global.exception.ErrorCode.INVALID_ACCESS_TOKEN;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.user.entity.Role;
import com.example.earthtalk.global.exception.JwtCustomException;
import com.example.earthtalk.global.security.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT Token 관리 클래스
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private static final String AUTHORITIES_KEY = "authority";
    private final static String TOKEN_PREFIX = "Bearer ";

    public TokenResponse generateAllTokens(CustomOAuth2User oAuth2User, Date now) {
        String accessToken = generateAccessToken(oAuth2User, now);
        String refreshToken = generateRefreshToken(oAuth2User, now);

        return TokenResponse.from(accessToken, refreshToken);
    }

    // JWT Access Token 생성 메소드
    public String generateAccessToken(CustomOAuth2User oAuth2User, Date now) {
        // 토큰 생성하여 반환 (권한목록 추가)
        return Jwts.builder()
            .setSubject(oAuth2User.getEmail()) //사용자 이메일정보 활용
            .claim(AUTHORITIES_KEY, oAuth2User.getRole().toString()) // 권힌 claim 추가
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + jwtProperties.getAccessExpirationTime()))
            .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    // JWT Refresh Token 생성 메서드
    public String generateRefreshToken(CustomOAuth2User oAuth2User, Date now) {
        // 토큰 생성하여 반환
        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setSubject(oAuth2User.getEmail()) //사용자 이메일정보 활용
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + jwtProperties.getRefreshExpirationTime()))
            .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        String email = claims.getSubject();
        String role = claims.get(AUTHORITIES_KEY).toString();

        CustomOAuth2User customOAuth2User = getCustomOAuth2User(claims);

        return new UsernamePasswordAuthenticationToken(customOAuth2User, null,
            customOAuth2User.getAuthorities());
    }

    public CustomOAuth2User getCustomOAuth2User(Claims claims) {
        String email = claims.getSubject();
        String role = claims.get(AUTHORITIES_KEY).toString();

        return new CustomOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(role)),
            Map.of("email", email, "role", role), // 기본 attributes (OAuth2User에 필요)
            "email", // nameAttributeKey
            email,
            Role.valueOf(role)
        );
    }

    // JWT Access Token 유효성 검증 메서드
    public boolean validateAccessToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info(EXPIRED_ACCESS_TOKEN.getMessage());
            throw new JwtCustomException(EXPIRED_ACCESS_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.info(INVALID_ACCESS_TOKEN.getMessage());
            throw new JwtCustomException(INVALID_ACCESS_TOKEN);
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtProperties.getSecretKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String parseBearerToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}

