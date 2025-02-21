package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.global.constant.ContinentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsFilterRepository {

    Page<News> filterNews(String sort, ContinentType continent, String query, Pageable pageable);
}
