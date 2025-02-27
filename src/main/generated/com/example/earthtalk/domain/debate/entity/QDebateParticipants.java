package com.example.earthtalk.domain.debate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDebateParticipants is a Querydsl query type for DebateParticipants
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDebateParticipants extends EntityPathBase<DebateParticipants> {

    private static final long serialVersionUID = -1695943488L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDebateParticipants debateParticipants = new QDebateParticipants("debateParticipants");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDebate debate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<FlagType> position = createEnum("position", FlagType.class);

    public final EnumPath<DebateRole> role = createEnum("role", DebateRole.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.earthtalk.domain.user.entity.QUser user;

    public QDebateParticipants(String variable) {
        this(DebateParticipants.class, forVariable(variable), INITS);
    }

    public QDebateParticipants(Path<? extends DebateParticipants> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDebateParticipants(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDebateParticipants(PathMetadata metadata, PathInits inits) {
        this(DebateParticipants.class, metadata, inits);
    }

    public QDebateParticipants(Class<? extends DebateParticipants> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.debate = inits.isInitialized("debate") ? new QDebate(forProperty("debate"), inits.get("debate")) : null;
        this.user = inits.isInitialized("user") ? new com.example.earthtalk.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

