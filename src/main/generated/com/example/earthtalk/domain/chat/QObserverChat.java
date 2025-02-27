package com.example.earthtalk.domain.chat;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QObserverChat is a Querydsl query type for ObserverChat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QObserverChat extends EntityPathBase<ObserverChat> {

    private static final long serialVersionUID = -622338013L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QObserverChat observerChat = new QObserverChat("observerChat");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.example.earthtalk.domain.debate.entity.QDebate debate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.earthtalk.domain.user.entity.QUser user;

    public QObserverChat(String variable) {
        this(ObserverChat.class, forVariable(variable), INITS);
    }

    public QObserverChat(Path<? extends ObserverChat> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QObserverChat(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QObserverChat(PathMetadata metadata, PathInits inits) {
        this(ObserverChat.class, metadata, inits);
    }

    public QObserverChat(Class<? extends ObserverChat> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.debate = inits.isInitialized("debate") ? new com.example.earthtalk.domain.debate.entity.QDebate(forProperty("debate"), inits.get("debate")) : null;
        this.user = inits.isInitialized("user") ? new com.example.earthtalk.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

