package com.example.earthtalk.controller;

import com.example.earthtalk.domain.user.dto.response.UserBookmarksResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateChatsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateDetailsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebatesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFolloweesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFollowersResponse;
import com.example.earthtalk.domain.user.dto.response.UserLikesResponse;
import com.example.earthtalk.domain.user.dto.response.UserObserverChatsResponse;
import com.example.earthtalk.domain.user.service.UserService;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "JWT")
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

    @PostMapping("/mypage/{userId}/{newsId}/insertlike")
    public ResponseEntity<ApiResponse<Object>> insertlike(@PathVariable("newsId") Long newsId, @PathVariable("userId") Long userId) {

        int result = userService.insertLike(newsId, userId);

        if (result == 0) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("좋아요 삽입 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("좋아요 삽입 성공"));
        }
    }

    @GetMapping("/mypage/{userId}/bookmarks")
    public ResponseEntity<ApiResponse<List<UserBookmarksResponse>>> getUserBookmarks(@PathVariable("userId") Long userId) {
        List<UserBookmarksResponse> response = userService.getUserBookmarks( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    @PostMapping("/mypage/{userId}/{newsId}/insertBookmark")
    public ResponseEntity<ApiResponse<Object>> insertBookemark(@PathVariable("newsId") Long newsId, @PathVariable("userId") Long userId) {

        int result = userService.insertBookmark(newsId, userId);

        if (result == 0) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("북마크 삽입 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("북마크 삽입 성공"));
        }
    }

    @GetMapping("/mypage/{userId}/debates")
    public ResponseEntity<ApiResponse<List<UserDebatesResponse>>> getUserDebates(@PathVariable("userId") Long userId) {
        List<UserDebatesResponse> response = userService.getUserDebates( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }


    @GetMapping("/mypage/{debatesId}/debateChats")
    public ResponseEntity<ApiResponse<List<UserDebateChatsResponse>>> getUserDebateChats(
        @PathVariable("debatesId") Long debatesId) throws JsonProcessingException {
        List<UserDebateDetailsResponse> responseHeader = userService.getDebateDetails( debatesId );
        System.out.println("responseHeader: " + responseHeader);
        String headerData = new ObjectMapper().writeValueAsString(responseHeader);

        List<UserDebateChatsResponse> response = userService.getUserDebateChats( debatesId );
        return ResponseEntity.ok()
            .header("X-Debate-Details", headerData)  // 헤더에 데이터 추가
            .body(ApiResponse.createSuccess(response)); // 본문 데이터 설정
    }


    @GetMapping("/mypage/{userId}/followees")
    public ResponseEntity<ApiResponse<List<UserFolloweesResponse>>> getUserFollowersFollowees(@PathVariable("userId") Long userId) {
        List<UserFolloweesResponse> response = userService.getUserFollowees( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    @GetMapping("/mypage/{userId}/followers")
    public ResponseEntity<ApiResponse<List<UserFollowersResponse>>> getUserFollowersFollowers(@PathVariable("userId") Long userId) {
        List<UserFollowersResponse> response = userService.getUserFollowers( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    // 팔로워 추가
    @PostMapping("/mypage/{userId}/{followerId}/insertfollowers")
    public ResponseEntity<ApiResponse<Object>> insertUserFollowers(@PathVariable("userId") Long followeeId, @PathVariable("followerId") Long followerId) {

        int result = userService.insertUserFollows(followeeId, followerId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("팔로워 삽입 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("팔로워 삽입 성공"));
        }
    }

    // followee 추가
    @PostMapping("/mypage/{userId}/{followeeId}/insertfollowees")
    public ResponseEntity<ApiResponse<Object>> insertUserFollowees(@PathVariable("userId") Long followerId, @PathVariable("followeeId") Long followeeId) {

        int result = userService.insertUserFollows(followerId, followeeId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("팔로워 삽입 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("팔로워 삽입 성공"));
        }
    }

    @DeleteMapping("/mypage/{userId}/{followerId}/deletefollowers")
    public ResponseEntity<ApiResponse<Object>> deleteUserFollowers(@PathVariable("userId") Long followeeId, @PathVariable("followerId") Long followerId) {

        int result = userService.deleteUserFollows(followeeId, followerId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("팔로워 삭제 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("팔로워 삭제 성공"));
        }
    }


    @DeleteMapping("/mypage/{userId}/{followeeId}/deletefollowees")
    public ResponseEntity<ApiResponse<Object>> deleteUserFollowees(@PathVariable("userId") Long followerId, @PathVariable("followeeId") Long followeeId) {

        int result = userService.deleteUserFollows(followerId, followeeId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("followee 삭제 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("followee 삭제 성공"));
        }
    }

    @PutMapping("/mypage/{userId}")
    public ResponseEntity<ApiResponse<Object>> updateUserInfos(
        @RequestParam(required = false) String nickname,
        @RequestParam(required = false) String introduction,
        @RequestParam(required = false) String profileUrl,
        @PathVariable("userId") Long userId) {

        int result = userService.updateUsers(nickname, introduction, profileUrl, userId);

        if (result == 1) {
            // 업데이트 실패 시 400 BAD REQUEST 반환
            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("프로필 업데이트 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("프로필이 성공적으로 변경되었습니다."));
        }
    }
}
