package com.example.earthtalk.controller;

import com.example.earthtalk.domain.oauth.dto.CustomOAuth2User;
import com.example.earthtalk.domain.user.dto.response.UserBookmarksResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateChatsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebateDetailsResponse;
import com.example.earthtalk.domain.user.dto.response.UserDebatesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFolloweesResponse;
import com.example.earthtalk.domain.user.dto.response.UserFollowersResponse;
import com.example.earthtalk.domain.user.dto.response.UserLikesResponse;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.service.UserService;
import com.example.earthtalk.domain.user.dto.response.UserInfoResponse;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.JwtCustomException;
import com.example.earthtalk.global.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Slf4j
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


    @Operation(summary = "사용자 좋아요 목록 조회 API", description = "사용자 좋아요 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/mypage/{userId}/likes")
    public ResponseEntity<ApiResponse<List<UserLikesResponse>>> getUserLikes(@PathVariable("userId") Long userId) {
        List<UserLikesResponse> response = userService.getUserLikes( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }


    @Operation(summary = "사용자 좋아요 추가 API", description = "뉴스 기사에 대한 좋아요를 추가합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @PostMapping("/mypage/{userId}/{newsId}/insertlike")
    public ResponseEntity<ApiResponse<Object>> insertlike(@PathVariable("newsId") Long newsId, @PathVariable("userId") Long userId) {

        int result = userService.insertLike(newsId, userId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("좋아요 삽입 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("좋아요 삽입 성공"));
        }
    }

    @Operation(summary = "사용자 좋아요 삭제 API", description = "뉴스 기사에 대한 좋아요를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @DeleteMapping("/mypage/{userId}/{newsId}/deletelike")
    public ResponseEntity<ApiResponse<Object>> deletelike(@PathVariable("newsId") Long newsId, @PathVariable("userId") Long userId) {

        int result = userService.deleteLike(newsId, userId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("좋아요 삭제 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("좋아요 삭제 성공"));
        }
    }

    @Operation(summary = "사용자 북마크 목록 조회 API", description = "사용자 북마크 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/mypage/{userId}/bookmarks")
    public ResponseEntity<ApiResponse<List<UserBookmarksResponse>>> getUserBookmarks(@PathVariable("userId") Long userId) {
        List<UserBookmarksResponse> response = userService.getUserBookmarks( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    @Operation(summary = "사용자 북마크 추가 API", description = "뉴스 기사에 대한 북마크를 추가합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @PostMapping("/mypage/{userId}/{newsId}/insertBookmark")
    public ResponseEntity<ApiResponse<Object>> insertBookemark(@PathVariable("newsId") Long newsId, @PathVariable("userId") Long userId) {

        int result = userService.insertBookmark(newsId, userId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("북마크 삽입 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("북마크 삽입 성공"));
        }
    }

    @Operation(summary = "사용자 북마크 삭제 API", description = "뉴스 기사에 대한 북마크를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @DeleteMapping("/mypage/{userId}/{newsId}/deleteBookmark")
    public ResponseEntity<ApiResponse<Object>> deleteBookmark(@PathVariable("newsId") Long newsId, @PathVariable("userId") Long userId) {

        int result = userService.deleteBookmark(newsId, userId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("북마크 삭제 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("북마크 삭제 성공"));
        }
    }

    @Operation(summary = "사용자 참여/참관 토론방 목록 조회 API", description = "사용자가 참여하거나 참관한 토론방 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/mypage/{userId}/debates")
    public ResponseEntity<ApiResponse<List<UserDebatesResponse>>> getUserDebates(@PathVariable("userId") Long userId) {
        List<UserDebatesResponse> response = userService.getUserDebates( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }


    @Operation(summary = "토론방 상세 정보 조회 API", description = "토론방 채팅 기록을 비롯한 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
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


    @Operation(summary = "사용자 팔로잉 목록 API", description = "사용자가 팔로우하고 있는 사용자 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/mypage/{userId}/followees")
    public ResponseEntity<ApiResponse<List<UserFolloweesResponse>>> getUserFollowersFollowees(@PathVariable("userId") Long userId) {
        List<UserFolloweesResponse> response = userService.getUserFollowees( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    @Operation(summary = "사용자 팔로워 목록 조회 API", description = "사용자의 팔로워 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/mypage/{userId}/followers")
    public ResponseEntity<ApiResponse<List<UserFollowersResponse>>> getUserFollowersFollowers(@PathVariable("userId") Long userId) {
        List<UserFollowersResponse> response = userService.getUserFollowers( userId );
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
    }

    // 팔로워 추가
    @Operation(summary = "사용자 팔로워 추가 API", description = "사용자의 팔로워를 추가합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
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
    @Operation(summary = "사용자 팔로잉 추가 API", description = "팔로우하는 사용자를 추가합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @PostMapping("/mypage/{userId}/{followeeId}/insertfollowees")
    public ResponseEntity<ApiResponse<Object>> insertUserFollowees(@PathVariable("userId") Long followerId, @PathVariable("followeeId") Long followeeId) {

        int result = userService.insertUserFollows(followeeId, followerId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("followee 삽입 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("followee 삽입 성공"));
        }
    }

    @Operation(summary = "사용자 팔로워 삭제 API", description = "사용자의 팔로워를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
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


    @Operation(summary = "사용자 팔로잉 삭제 API", description = "팔로우하는 사용자를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @DeleteMapping("/mypage/{userId}/{followeeId}/deletefollowees")
    public ResponseEntity<ApiResponse<Object>> deleteUserFollowees(@PathVariable("userId") Long followerId, @PathVariable("followeeId") Long followeeId) {

        int result = userService.deleteUserFollows(followeeId, followerId);

        if (result == 1) {

            return ResponseEntity.badRequest()
                .body(ApiResponse.createErrorWithMsg("followee 삭제 실패"));
        } else {

            return ResponseEntity.ok()
                .body(ApiResponse.createSuccess("followee 삭제 성공"));
        }
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 프로필을 업데이트합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
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
