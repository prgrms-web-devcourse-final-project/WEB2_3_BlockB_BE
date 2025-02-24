package com.example.earthtalk.controller;

import com.example.earthtalk.domain.news.dto.NewsDetailDTO;
import com.example.earthtalk.domain.news.dto.NewsListDTO;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.service.NewsDataService;
import com.example.earthtalk.domain.news.service.NewsService;
import com.example.earthtalk.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class NewsController {

    private final NewsService newsService;
    private final NewsDataService newsDataService;

    /***
     * 예시 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getNewsList(
        @RequestParam(value = "continent", required = false) String continent,
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "cursor", required = false) Long newsId) {
        Slice<NewsListDTO> data = newsDataService
            .getNewsByFilter(continent, query, sort, newsId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(data));
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<Object>> getNewsById(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        NewsDetailDTO data = newsDataService.getNewsDetail(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(data));
    }

    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<Object>> getNewsRanking() {
        List<NewsListDTO> data = newsDataService.getNewsRanking();
        return ResponseEntity.ok().body(ApiResponse.createSuccess(data));
    }

    @PostMapping("/{newsId}/like")
    public ResponseEntity<ApiResponse<Object>> likeNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.addLike(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @DeleteMapping("/{newsId}/like")
    public ResponseEntity<ApiResponse<Object>> unlikeNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.removeLike(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @PostMapping("/{newsId}/bookmark")
    public ResponseEntity<ApiResponse<Object>> bookmarkNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.addBookmark(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @DeleteMapping("/{newsId}/bookmark")
    public ResponseEntity<ApiResponse<Object>> unmarkNews(
        @PathVariable("newsId") Long newsId,
        @RequestParam("userId") Long userId) {
        newsDataService.removeBookmark(newsId, userId);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }
}