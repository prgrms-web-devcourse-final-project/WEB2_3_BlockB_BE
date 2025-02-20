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
    public List<Tuple> findAllWithFollowCountOrderBy() {
        QUser user = QUser.user;
        QFollow follow1 = new QFollow("follow1");
        QFollow follow2 = new QFollow("follow2");

        JPQLQuery<Tuple> query = jpaQueryFactory
            .select(
                user.id,
                follow1.follower.id.countDistinct().coalesce(0L),
                follow2.followee.id.countDistinct().coalesce(0L)
            )
            .from(user)
            .leftJoin(follow1).on(user.id.eq(follow1.followee.id))
            .leftJoin(follow2).on(user.id.eq(follow2.follower.id))
            .groupBy(user.id)
            .orderBy(follow1.follower.id.countDistinct().desc());

        return query.fetch();
    }
}
