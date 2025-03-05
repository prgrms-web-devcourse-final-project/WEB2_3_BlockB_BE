package com.example.earthtalk.controller;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.oauth.dto.request.TokenRequest;
import com.example.earthtalk.domain.oauth.service.OAuth2Service;
import com.example.earthtalk.domain.user.dto.request.UserInfoRequest;
import com.example.earthtalk.domain.user.service.UserService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "🔐 Auth", description = "인증 관련 API")
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final UserService userService;

    @Operation(summary = "토큰 재발급 API", description = "Refresh 토큰을 통해 Access 토큰, Refresh 토큰 모두 재발급합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")
    })
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse.GetToken>> getReissue(@RequestBody TokenRequest tokenRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccess(oAuth2Service.getReissue(tokenRequest.refreshToken())));
    }

    @Operation(summary = "닉네임 검증 API", description = "닉네임 중복을 확인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/nickname-confirm")
    public ResponseEntity<ApiResponse<Object>> confirmNickname(@RequestBody UserInfoRequest.ConfirmNickname userInfoRequest,
        @AuthenticationPrincipal CustomOAuth2User user) {
        oAuth2Service.confirmNickname(userInfoRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccessWithNoData());
    }

    @Operation(summary = "회원가입 API", description = "닉네임 중복확인 후 회원가입을 완료합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Object>> oauthSignup(@RequestBody UserInfoRequest.Signup userInfoRequest,
        @AuthenticationPrincipal CustomOAuth2User user) {
        oAuth2Service.completeSignup(userInfoRequest, user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccessWithNoData());
    }

    @Operation(summary = "회원 탈퇴 API", description = "사용자 회원을 탈퇴하고 정보를 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<Object>> oauthWithdraw(@AuthenticationPrincipal CustomOAuth2User user) {

        userService.deleteMember(user.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.createSuccessWithNoData());
    }

}
