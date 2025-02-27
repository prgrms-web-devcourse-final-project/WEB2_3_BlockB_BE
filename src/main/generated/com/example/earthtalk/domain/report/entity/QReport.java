package com.example.earthtalk.domain.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReport is a Querydsl query type for Report
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReport extends EntityPathBase<Report> {

    private static final long serialVersionUID = 1976847430L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReport report = new QReport("report");

    public final com.example.earthtalk.global.baseTime.QBaseTimeEntity _super = new com.example.earthtalk.global.baseTime.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath reportContent = createString("reportContent");

    public final DateTimePath<java.time.LocalDateTime> reportedAt = createDateTime("reportedAt", java.time.LocalDateTime.class);

    public final EnumPath<ReportType> reportType = createEnum("reportType", ReportType.class);

    public final EnumPath<ResultType> resultType = createEnum("resultType", ResultType.class);

    public final NumberPath<Long> targetRoomId = createNumber("targetRoomId", Long.class);

    public final EnumPath<TargetType> targetType = createEnum("targetType", TargetType.class);

    public final com.example.earthtalk.domain.user.entity.QUser targetUser;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.earthtalk.domain.user.entity.QUser user;

    public QReport(String variable) {
        this(Report.class, forVariable(variable), INITS);
    }

    public QReport(Path<? extends Report> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReport(PathMetadata metadata, PathInits inits) {
        this(Report.class, metadata, inits);
    }

    public QReport(Class<? extends Report> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.targetUser = inits.isInitialized("targetUser") ? new com.example.earthtalk.domain.user.entity.QUser(forProperty("targetUser")) : null;
        this.user = inits.isInitialized("user") ? new com.example.earthtalk.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

