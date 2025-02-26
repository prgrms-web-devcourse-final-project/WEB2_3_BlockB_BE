package com.example.earthtalk.domain.user.repository;

import static com.example.earthtalk.domain.debate.entity.DebateRole.OBSERVER;
import static com.example.earthtalk.domain.debate.entity.DebateRole.PARTICIPANT;

import com.example.earthtalk.domain.debate.entity.QDebate;
import com.example.earthtalk.domain.debate.entity.QDebateChat;
import com.example.earthtalk.domain.debate.entity.QDebateParticipants;
import com.example.earthtalk.domain.news.entity.QBookmark;
import com.example.earthtalk.domain.news.entity.QLike;
import com.example.earthtalk.domain.news.entity.QNews;
import com.example.earthtalk.domain.user.entity.QFollow;
import com.example.earthtalk.domain.user.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
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
        QNews news = QNews.news;

        JPQLQuery<Tuple> jpaQuery = jpaQueryFactory
            .select(like.user.id,
                news.id.as("newsId"),
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
                news.title,
                news.continent,
                bookmark.createdAt)
            .from(bookmark)
            .rightJoin(news).on(news.id.eq(bookmark.news.id))
            .where(bookmark.user.id.eq(userId));


        return jpaQuery.fetch();
    }


    // 유저가 참관/참여한 토론방 조회
    public List<Tuple> findAllWithDebates(Long userId) {
        QDebateChat dc = QDebateChat.debateChat;
        QDebateParticipants dp = QDebateParticipants.debateParticipants;
        QDebate debate = QDebate.debate;

        JPQLQuery<Tuple> result = jpaQueryFactory
            .select(debate.id, debate.category, debate.title, debate.time, debate.member, debate.status,
                new CaseBuilder()
                .when(dp.role.eq(PARTICIPANT)).then(true)
                .when(dp.role.eq(OBSERVER)).then(false)
                .otherwise(false)
                .as("is_participant")
)
            .from(debate)
            .leftJoin(dp).on(debate.id.eq(dp.debate.id))
            .where(dp.user.id.eq(userId));

        return result.fetch();
    }


    // 유저가 참관/참여한 토론방 상세 조회 - header
    public List<Tuple> findAllWithDebateDetails(Long debatesId) {
        QDebate d = QDebate.debate;
        QNews n = QNews.news;

        List<Tuple> result = jpaQueryFactory
            .select(
                d.id,
                d.title,
                d.description,
                n.link,
                d.continent,
                d.category,
                d.member,
                d.time,
                d.agreeNumber,
                d.disagreeNumber
            )
            .from(d)
            .join(n).on(n.id.eq(d.news.id))
            .where(d.id.eq(debatesId))
            .fetch();

        return result;
    }


    // 유저가 참관/참여한 토론방 상세 조회
    public List<Tuple> findAllWithDebateChats(Long debatesId) {
        QDebateChat dc = QDebateChat.debateChat;
        QDebateParticipants dp = QDebateParticipants.debateParticipants;

        List<Tuple> result = jpaQueryFactory
            .select(
                dp.user.id,
                dp.role,
                dp.position,
                dc.content.as("debateContent"),
                dc.createdAt.as("chatCreatedAt"))
            .from(dc)
            .join(dp).on(dc.debate.id.eq(dp.debate.id)
                .and(dc.debateParticipants.id.eq(dp.user.id)))
            .where(dc.debate.id.eq(debatesId))
            .orderBy(dc.createdAt.asc())
            .fetch();

        return result;
    }


    // 유저 followee 목록
    public List<Tuple> findAllWithFollowees(Long userId) {
        QUser user = QUser.user;
        QFollow follow = QFollow.follow;

        JPQLQuery<Tuple> jpaQuery = jpaQueryFactory
            .select(follow.followee.id, user.nickname, user.profileUrl, user.introduction)
            .from(follow)
            .innerJoin(user).on(user.id.eq(follow.followee.id))
            .where(follow.follower.id.eq(userId));

        return jpaQuery.fetch();
    }


    // 유저 팔로워 목록
    public List<Tuple> findAllWithFollowers(Long userId) {
        QUser user = QUser.user;
        QFollow follow = QFollow.follow;

        JPQLQuery<Tuple> jpaQuery = jpaQueryFactory
            .select(follow.follower.id, user.nickname, user.profileUrl, user.introduction)
            .from(follow)
            .innerJoin(user).on(user.id.eq(follow.follower.id))
            .where(follow.followee.id.eq(userId));

        return jpaQuery.fetch();
    }

}
