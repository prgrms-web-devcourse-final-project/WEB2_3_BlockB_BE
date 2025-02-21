package com.example.earthtalk.domain.user.repository;

import com.example.earthtalk.domain.debate.entity.QDebate;
import com.example.earthtalk.domain.debate.entity.QDebateUser;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.news.entity.QBookmark;
import com.example.earthtalk.domain.news.entity.QLike;
import com.example.earthtalk.domain.news.entity.QNews;
import com.example.earthtalk.domain.user.entity.QFollow;
import com.example.earthtalk.domain.user.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Tuple> findAllWithFollowCountOrderBy(String query) {
        QUser user = QUser.user;
        QFollow follow1 = new QFollow("follow1");
        QFollow follow2 = new QFollow("follow2");

        JPQLQuery<Tuple> jpaQuery = jpaQueryFactory
            .select(
                user.id,
                follow1.follower.id.countDistinct().coalesce(0L),
                follow2.followee.id.countDistinct().coalesce(0L)
            )
            .from(user)
            .leftJoin(follow1).on(user.id.eq(follow1.followee.id))
            .leftJoin(follow2).on(user.id.eq(follow2.follower.id))
            .groupBy(user.id);

        // query 파라미터가 null이 아니거나 빈칸이 아니면 검색어 포함 닉네임의 유저 검색 조건 추가
        // 대소문자를 무시한 부분 일치 검색
        if (query != null && !query.trim().isEmpty()) {
            jpaQuery.where(user.nickname.containsIgnoreCase(query));
        }

        // 인기순으로 정렬 (팔로워 수 기준)
        jpaQuery.orderBy(follow1.follower.id.countDistinct().desc());

        return jpaQuery.fetch();
    }

    public List<Tuple> findAllWithLikes(Long userId) {
        QLike like = QLike.like;
        QNews news = QNews.news;  // QNews 객체 추가

        JPQLQuery<Tuple> jpaQuery = jpaQueryFactory
            .select(like.user.id,
                news.id.as("newsId"),
                like.isLike,
                news.title,
                news.continent,
                like.createdAt)
            .from(like)
            .rightJoin(news).on(news.id.eq(like.news.id))
            .where(like.user.id.eq(userId));

        return jpaQuery.fetch();
    }

    public List<Tuple> findAllWithBookmarks(Long userId) {
        QBookmark bookmark = QBookmark.bookmark;
        QNews news = QNews.news;  // QNews 객체 추가

        JPQLQuery<Tuple> jpaQuery = jpaQueryFactory
            .select(bookmark.user.id,
                news.id.as("newsId"),
                bookmark.isBookmarked,
                news.title,
                news.continent,
                bookmark.createdAt)
            .from(bookmark)
            .rightJoin(news).on(news.id.eq(bookmark.news.id))
            .where(bookmark.user.id.eq(userId));

        return jpaQuery.fetch();
    }

    public List<Tuple> findAllWithDebates(Long userId) {
        QDebate debate = QDebate.debate;
        QDebateUser du = QDebateUser.debateUser;

        JPQLQuery<Tuple> jpaQuery = jpaQueryFactory
            .select(debate.id,
                debate.category,
                debate.title,
                debate.time,
                debate.member,
                du.user.id,
                debate.status)
            .from(debate)
            .innerJoin(du).on(debate.id.eq(du.debate.id))
            .where(du.user.id.eq(userId));


        return jpaQuery.fetch();
    }
}
