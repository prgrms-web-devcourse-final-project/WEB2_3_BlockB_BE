package com.example.earthtalk.domain.debate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDebate is a Querydsl query type for Debate
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDebate extends EntityPathBase<Debate> {

    private static final long serialVersionUID = 152316160L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDebate debate = new QDebate("debate");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    public final NumberPath<Long> agreeNumber = createNumber("agreeNumber", Long.class);

    public final EnumPath<CategoryType> category = createEnum("category", CategoryType.class);

    public final EnumPath<com.example.earthtalk.global.constant.ContinentType> continent = createEnum("continent", com.example.earthtalk.global.constant.ContinentType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> disagreeNumber = createNumber("disagreeNumber", Long.class);

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.example.earthtalk.domain.news.entity.MemberNumberType> member = createEnum("member", com.example.earthtalk.domain.news.entity.MemberNumberType.class);

    public final NumberPath<Long> neutralNumber = createNumber("neutralNumber", Long.class);

    public final com.example.earthtalk.domain.news.entity.QNews news;

    public final ListPath<DebateParticipants, QDebateParticipants> participants = this.<DebateParticipants, QDebateParticipants>createList("participants", DebateParticipants.class, QDebateParticipants.class, PathInits.DIRECT2);

    public final BooleanPath resultEnabled = createBoolean("resultEnabled");

    public final EnumPath<SpeakCountType> speakCount = createEnum("speakCount", SpeakCountType.class);

    public final EnumPath<RoomType> status = createEnum("status", RoomType.class);

    public final EnumPath<com.example.earthtalk.domain.news.entity.TimeType> time = createEnum("time", com.example.earthtalk.domain.news.entity.TimeType.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final ComparablePath<java.util.UUID> uuid = createComparable("uuid", java.util.UUID.class);

    public QDebate(String variable) {
        this(Debate.class, forVariable(variable), INITS);
    }

    public QDebate(Path<? extends Debate> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDebate(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDebate(PathMetadata metadata, PathInits inits) {
        this(Debate.class, metadata, inits);
    }

    public QDebate(Class<? extends Debate> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.news = inits.isInitialized("news") ? new com.example.earthtalk.domain.news.entity.QNews(forProperty("news")) : null;
    }

}

