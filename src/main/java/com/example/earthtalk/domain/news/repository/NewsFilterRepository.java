package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.dto.response.NewsListResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

public interface NewsFilterRepository {

    List<NewsListResponse> filterNews(BooleanBuilder whereBuilder, BooleanBuilder havingBuilder,
        OrderSpecifier<?> orderSpecifier, int pageSize);

    List<NewsListResponse> newsRanking();

    boolean isLiked(Long userId, Long newsId);

    boolean isMarked(Long userId, Long newsId);
}
