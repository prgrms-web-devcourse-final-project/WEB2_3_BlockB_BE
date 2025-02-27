package com.example.earthtalk.domain.debate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDebateChat is a Querydsl query type for DebateChat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDebateChat extends EntityPathBase<DebateChat> {

    private static final long serialVersionUID = -1594380168L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDebateChat debateChat = new QDebateChat("debateChat");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDebate debate;

    public final QDebateParticipants debateParticipants;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> time = createDateTime("time", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDebateChat(String variable) {
        this(DebateChat.class, forVariable(variable), INITS);
    }

    public QDebateChat(Path<? extends DebateChat> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDebateChat(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDebateChat(PathMetadata metadata, PathInits inits) {
        this(DebateChat.class, metadata, inits);
    }

    public QDebateChat(Class<? extends DebateChat> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.debate = inits.isInitialized("debate") ? new QDebate(forProperty("debate"), inits.get("debate")) : null;
        this.debateParticipants = inits.isInitialized("debateParticipants") ? new QDebateParticipants(forProperty("debateParticipants"), inits.get("debateParticipants")) : null;
    }

}

