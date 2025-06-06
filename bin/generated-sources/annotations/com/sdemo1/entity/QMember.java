package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 1887561439L;

    public static final QMember member = new QMember("member1");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final DateTimePath<java.sql.Timestamp> modifiedAt = createDateTime("modifiedAt", java.sql.Timestamp.class);

    public final StringPath name = createString("name");

    public final StringPath profileImg = createString("profileImg");

    public final EnumPath<Member.Provider> provider = createEnum("provider", Member.Provider.class);

    public final StringPath providerId = createString("providerId");

    public final EnumPath<Member.Role> role = createEnum("role", Member.Role.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final StringPath userLoginId = createString("userLoginId");

    public final StringPath userLoginPw = createString("userLoginPw");

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

