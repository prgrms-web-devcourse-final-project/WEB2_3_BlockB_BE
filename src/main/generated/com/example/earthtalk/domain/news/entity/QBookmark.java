package com.example.earthtalk.domain.news.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookmark is a Querydsl query type for Bookmark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookmark extends EntityPathBase<Bookmark> {

    private static final long serialVersionUID = -1631345753L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookmark bookmark = new QBookmark("bookmark");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QNews news;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.earthtalk.domain.user.entity.QUser user;

    public QBookmark(String variable) {
        this(Bookmark.class, forVariable(variable), INITS);
    }

    public QBookmark(Path<? extends Bookmark> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookmark(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookmark(PathMetadata metadata, PathInits inits) {
        this(Bookmark.class, metadata, inits);
    }

    public QBookmark(Class<? extends Bookmark> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.news = inits.isInitialized("news") ? new QNews(forProperty("news")) : null;
        this.user = inits.isInitialized("user") ? new com.example.earthtalk.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

