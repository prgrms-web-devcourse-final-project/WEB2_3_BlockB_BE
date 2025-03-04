package com.example.earthtalk.controller;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.oauth.service.OAuth2Service;
import com.example.earthtalk.domain.user.dto.request.UserInfoRequest;
import com.example.earthtalk.global.response.ApiResponse;
import com.example.earthtalk.domain.oauth.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "🔐 Auth", description = "인증 관련 API")
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final String AUTHORIZATION_HEADER = "Authorization";

    @Operation(summary = "토큰 재발급 API", description = "Refresh 토큰을 통해 Access 토큰, Refresh 토큰 모두 재발급합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")
    })
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse.GetToken>> getReissue(
        @RequestHeader(AUTHORIZATION_HEADER) String refreshToken) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccess(oAuth2Service.getReissue(refreshToken)));
    }

    @Operation(summary = "닉네임 검증후 회원가입 API", description = "닉네임 중복확인 후 회원가입을 완료합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")
    })
    @PostMapping("/complete-signup")
    public ResponseEntity<ApiResponse<Object>> oauthSignup(UserInfoRequest userInfoRequest,
        @AuthenticationPrincipal CustomOAuth2User user) {
        oAuth2Service.completeSignup(userInfoRequest, user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccessWithNoData());
    }
}
