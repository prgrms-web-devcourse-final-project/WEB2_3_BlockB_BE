package com.example.earthtalk.domain.news.repository;

import static com.example.earthtalk.domain.news.entity.QBookmark.bookmark;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class BookmarkRepositoryImpl implements BookmarkRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    public BookmarkRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public boolean existsByUserIdAndNewsId(Long userId, Long newsId) {

        return jpaQueryFactory
            .selectFrom(bookmark)
            .where(bookmark.user.id.eq(userId)
                .and(bookmark.news.id.eq(newsId)))
            .fetchCount() > 0;
    }
}
