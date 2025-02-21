package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.global.constant.ContinentType;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NewsFilterRepositoryImpl implements NewsFilterRepository {

    private final JPQLQueryFactory jpqlQueryFactory;

    @Override
    public Page<News> filterNews(String sort, ContinentType continent, String query,
        Pageable pageable) {
        return null;
    }
}
