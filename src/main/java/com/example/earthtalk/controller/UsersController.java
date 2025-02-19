package com.example.earthtalk.controller;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.service.NewsService;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.service.UserService;
import com.example.earthtalk.domain.user.userDto.UserInfoDto;
import com.example.earthtalk.global.response.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<Object>> getUserInfo() {
        List<UserInfoDto> response = userService.getPopularUsersInfo();
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }
}
