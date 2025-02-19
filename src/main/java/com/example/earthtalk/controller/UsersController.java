package com.example.earthtalk.controller;

import com.example.earthtalk.domain.user.service.UserService;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UsersController {

    private final UserService userService;

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getUserInfo() {
        List<UserInfoResponse> response = userService.getPopularUsersInfo();
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }
}
