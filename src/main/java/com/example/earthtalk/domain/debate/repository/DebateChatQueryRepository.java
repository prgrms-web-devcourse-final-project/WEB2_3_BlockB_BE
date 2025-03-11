package com.example.earthtalk.domain.debate.repository;

import com.example.earthtalk.domain.chat.QObserverChat;
import com.example.earthtalk.domain.debate.entity.QDebateChat;
import com.example.earthtalk.domain.debate.entity.QDebateParticipants;
import com.example.earthtalk.domain.user.dto.response.DebateChatResponse;
import com.example.earthtalk.domain.user.dto.response.ObserverChatResponse;
import com.example.earthtalk.domain.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DebateChatQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<DebateChatResponse> findDebateChatsByDebateId(Long debateId) {
        QDebateChat debateChat = QDebateChat.debateChat;
        QDebateParticipants debateParticipants = QDebateParticipants.debateParticipants;
        QUser user = QUser.user;

        return queryFactory
            .select(Projections.constructor(DebateChatResponse.class,
                user.nickname,
                debateParticipants.position,
                debateChat.content,
                user.profileUrl,
                debateChat.createdAt))
            .from(debateChat)
            .join(debateParticipants).on(debateChat.debateParticipants.id.eq(debateParticipants.id))
            .join(user).on(debateParticipants.user.id.eq(user.id))
            .where(debateChat.debate.id.eq(debateId))
            .orderBy(debateChat.createdAt.desc())
            .fetch();
    }

    public List<ObserverChatResponse> findObserverChatsByDebateId(Long debateId) {
        QObserverChat observerChat = QObserverChat.observerChat;
        QUser user = QUser.user;

        return queryFactory
            .select(Projections.constructor(ObserverChatResponse.class,
                user.nickname,
                observerChat.content,
                user.profileUrl,
                observerChat.createdAt))
            .from(observerChat)
            .join(user).on(observerChat.user.id.eq(user.id))
            .where(observerChat.debate.id.eq(debateId))
            .orderBy(observerChat.createdAt.desc())
            .fetch();
    }
}
