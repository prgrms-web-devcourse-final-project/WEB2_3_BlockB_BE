package com.example.earthtalk.controller;

import com.example.earthtalk.domain.news.dto.response.NewsDetailReponse;
import com.example.earthtalk.domain.news.dto.response.NewsListResponse;
import com.example.earthtalk.domain.news.service.NewsDataService;
import com.example.earthtalk.domain.news.service.NewsService;
import com.example.earthtalk.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@Tag(name = "📰 News", description = "뉴스 관련 API")
public class NewsController {

    private final NewsService newsService;
    private final NewsDataService newsDataService;


    @Operation(summary = "모든 뉴스 조회 및 필터링 API", description = "스크롤 해서 새로운 뉴스를 요청할 때 cursor 에 현재까지 로드된 뉴스 중 마지막 뉴스의 id를 주시면 됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getNewsList(
        @RequestParam(value = "continent", required = false) String continent,
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "cursor", required = false) Long newsId) {
        Slice<NewsListResponse> data = newsDataService
            .getNewsByFilter(continent, query, sort, newsId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(data));
    }

    @Operation(summary = "뉴스 상세보기 API입니다.", description = "사용자 id를 요청에 포함시키면 사용자가 해당 뉴스에 좋아요,북마크를 이미 눌렀는지에 대한 정보를 추가로 반환합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<Object>> getNewsById(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        NewsDetailReponse data = newsDataService.getNewsDetail(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(data));
    }

    @Operation(summary = "뉴스 Top10 API 입니다.", description = "뉴스 조회하기와 같은 형식의 데이터를 반환합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<Object>> getNewsRanking() {
        List<NewsListResponse> data = newsDataService.getNewsRanking();
        return ResponseEntity.ok().body(ApiResponse.createSuccess(data));
    }

    @Operation(summary = "좋아요 추가 API 입니다.", description = "뉴스아이디와 사용자 아이디를 기반으로 좋아요를 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")})
    @PostMapping("/{newsId}/like")
    public ResponseEntity<ApiResponse<Object>> likeNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.addLike(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @Operation(summary = "좋아요 취소 API 입니다.", description = "생성돼있던 좋아요를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")})
    @DeleteMapping("/{newsId}/like")
    public ResponseEntity<ApiResponse<Object>> unlikeNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.removeLike(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @Operation(summary = "북마크 추가 API 입니다.", description = "뉴스아이디와 사용자 아이디를 기반으로 북마크를 추가합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")})
    @PostMapping("/{newsId}/bookmark")
    public ResponseEntity<ApiResponse<Object>> bookmarkNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.addBookmark(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @Operation(summary = "북마크 취소 API 입니다.", description = "생성돼있던 북마크를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공")})
    @DeleteMapping("/{newsId}/bookmark")
    public ResponseEntity<ApiResponse<Object>> unmarkNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.removeBookmark(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }
}