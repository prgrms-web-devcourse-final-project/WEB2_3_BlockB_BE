package com.example.earthtalk.controller;

import com.example.earthtalk.domain.oauth.service.OAuth2Service;
import com.example.earthtalk.global.response.ApiResponse;
import com.example.earthtalk.domain.oauth.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "🔑 OAuth2", description = "소셜로그인 관련 API")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @Operation(summary = "인가 코드 전달 API", description = "로그인에 필요한 인가 코드를 서버에 전달합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")
    })
    @PostMapping("/login/oauth2/callback")
    public ResponseEntity<ApiResponse<TokenResponse.GetOauth>> getAuthorizationCode(HttpServletRequest request, HttpServletResponse response,
        @RequestParam("code") String code, @RequestParam("provider") String provider) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccess(oAuth2Service.getResource(request, response, provider, code)));
    }
}
