package com.example.earthtalk.domain.news.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNews is a Querydsl query type for News
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNews extends EntityPathBase<News> {

    private static final long serialVersionUID = 483027396L;

    public static final QNews news = new QNews("news");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    public final EnumPath<com.example.earthtalk.global.constant.ContinentType> continent = createEnum("continent", com.example.earthtalk.global.constant.ContinentType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deliveryTime = createDateTime("deliveryTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imgUrl = createString("imgUrl");

    public final StringPath link = createString("link");

    public final EnumPath<NewsType> newsType = createEnum("newsType", NewsType.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QNews(String variable) {
        super(News.class, forVariable(variable));
    }

    public QNews(Path<? extends News> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNews(PathMetadata metadata) {
        super(News.class, metadata);
    }

}

