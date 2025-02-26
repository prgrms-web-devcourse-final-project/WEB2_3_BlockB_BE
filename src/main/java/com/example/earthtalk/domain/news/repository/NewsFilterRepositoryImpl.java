package com.example.earthtalk.domain.news.repository;

import com.example.earthtalk.domain.news.dto.response.NewsListResponse;
import com.example.earthtalk.domain.news.entity.QBookmark;
import com.example.earthtalk.domain.news.entity.QLike;
import com.example.earthtalk.domain.news.entity.QNews;
import com.querydsl.core.BooleanBuilder;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsFilterRepositoryImpl implements NewsFilterRepository {

    private final NewsRepository newsRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private static final int PAGE_SIZE = 12;

    @Override
    public List<NewsListResponse> filterNews(BooleanBuilder whereBuilder,
        BooleanBuilder havingBuilder,
        OrderSpecifier<?> orderSpecifier, int pageSize) {
        QNews news = QNews.news;
        QLike like = QLike.like;
        QBookmark bookmark = QBookmark.bookmark;

        List<NewsListResponse> newsList = jpaQueryFactory
            .select(Projections.constructor(NewsListResponse.class,
                news, like.count(), bookmark.count()))
            .from(news)
            .leftJoin(like).on(news.id.eq(like.news.id))
            .leftJoin(bookmark).on(news.id.eq(bookmark.news.id))
            .where(whereBuilder)
            .having(havingBuilder)
            .groupBy(news.id)
            .orderBy(orderSpecifier) // sort 쿼리파라미터에 따라 다르게 정렬
            .orderBy(news.id.desc()) // 비교값이 같을 경우 id 순서로 정렬
            .limit(PAGE_SIZE + 1)
            .fetch();

        return newsList;
    }

    @Override
    public List<NewsListResponse> newsRanking() {
        QNews news = QNews.news;
        QBookmark bookmark = QBookmark.bookmark;
        QLike like = QLike.like;
        return jpaQueryFactory
            .select(Projections.constructor(NewsListResponse.class,
                news, like.count(), bookmark.count()))
            .from(news)
            .leftJoin(like).on(news.id.eq(like.news.id))
            .leftJoin(bookmark).on(news.id.eq(bookmark.news.id))
            .groupBy(news.id)
            .orderBy(like.count().desc())
            .limit(10)
            .fetch();
    }

    @Override
    public boolean isLiked(Long userId, Long newsId) {
        QLike like = QLike.like;
        Integer result = jpaQueryFactory
            .selectOne()
            .from(like)
            .where(like.user.id.eq(userId).and(like.news.id.eq(newsId)))
            .fetchFirst();
        return result != null;
    }

    @Override
    public boolean isMarked(Long userId, Long newsId) {
        QBookmark mark = QBookmark.bookmark;
        Integer result = jpaQueryFactory
            .selectOne()
            .from(mark)
            .where(mark.user.id.eq(userId).and(mark.news.id.eq(newsId)))
            .fetchFirst();
        return result != null;
    }

}
