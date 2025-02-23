package com.example.earthtalk.controller;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.service.UserService;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "😉 User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 목록 인기순 조회 API", description = "사용자 목록을 팔로워 많은 순으로 정렬하여 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getUserInfo(@RequestParam(required = false) String q) {
        List<UserInfoResponse> response = userService.getPopularUsersInfo( q );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    /**
     * 사용자 인증 테스트 API
     */
    @GetMapping("/userInfo")
    public ResponseEntity<ApiResponse<User>> getUser(
        @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok()
            .body(ApiResponse.createSuccess(userService.getUser(user.getEmail())));
    }
}
