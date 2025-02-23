package com.example.earthtalk.controller;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.service.NewsService;
import com.example.earthtalk.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@Tag(name = "📰 News", description = "뉴스 관련 API")
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "뉴스 목록 조회 API", description = "뉴스 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getNews() {
        List<News> newsList = newsService.getAllNews();
        return ResponseEntity.ok().body(ApiResponse.createSuccess(newsList));
    }
}