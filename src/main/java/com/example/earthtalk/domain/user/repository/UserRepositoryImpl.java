package com.example.earthtalk.domain.user.repository;

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
}
