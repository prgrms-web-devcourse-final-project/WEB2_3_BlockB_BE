package com.example.earthtalk.controller;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.service.NewsService;
import com.example.earthtalk.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    /***
     * 예시 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getNews() {
        List<News> newsList = newsService.getAllNews();
        return ResponseEntity.ok().body(ApiResponse.createSuccess(newsList));
    }
}