package com.example.earthtalk.domain.news.repository;

import static com.example.earthtalk.domain.news.entity.QLike.like;

import com.querydsl.jpa.impl.JPAQueryFactory;

public class LikeRepositoryImpl implements LikeRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    public LikeRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public boolean existsByUserIdAndNewsId(Long userId, Long newsId) {

        return jpaQueryFactory
            .selectFrom(like)
            .where(like.user.id.eq(userId)
                .and(like.news.id.eq(newsId)))
            .fetchCount() > 0;
    }
}
