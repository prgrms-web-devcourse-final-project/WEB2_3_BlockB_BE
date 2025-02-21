package com.example.earthtalk.controller;

import com.example.earthtalk.domain.user.dto.response.UserBookmarksResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebatesResponse;
import com.example.earthtalk.domain.user.dto.response.UserLikesResponse;
import com.example.earthtalk.domain.user.service.UserService;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UsersController {

    private final UserService userService;

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getUserInfo(@RequestParam(required = false) String q) {
        List<UserInfoResponse> response = userService.getPopularUsersInfo( q );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    @GetMapping("/mypage/{userId}/likes")
    public ResponseEntity<ApiResponse<List<UserLikesResponse>>> getUserLikes(@PathVariable("userId") Long userId) {
        List<UserLikesResponse> response = userService.getUserLikes( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    @GetMapping("/mypage/{userId}/bookmarks")
    public ResponseEntity<ApiResponse<List<UserBookmarksResponse>>> getUserBookmarks(@PathVariable("userId") Long userId) {
        List<UserBookmarksResponse> response = userService.getUserBookmarks( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    @GetMapping("/mypage/{userId}/debates")
    public ResponseEntity<ApiResponse<List<UserDebatesResponse>>> getUserDebates(@PathVariable("userId") Long userId) {
        List<UserDebatesResponse> response = userService.getUserDebates( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }
}
