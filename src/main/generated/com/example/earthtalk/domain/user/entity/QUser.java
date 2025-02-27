package com.example.earthtalk.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1638889396L;

    public static final QUser user = new QUser("user");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> defeatNumber = createNumber("defeatNumber", Long.class);

    public final NumberPath<Long> drawNumber = createNumber("drawNumber", Long.class);

    public final StringPath email = createString("email");

    public final StringPath FCMToken = createString("FCMToken");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath introduction = createString("introduction");

    public final StringPath nickname = createString("nickname");

    public final StringPath profileUrl = createString("profileUrl");

    public final EnumPath<Role> role = createEnum("role", Role.class);

    public final StringPath socialId = createString("socialId");

    public final EnumPath<SocialType> socialType = createEnum("socialType", SocialType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> winNumber = createNumber("winNumber", Long.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

