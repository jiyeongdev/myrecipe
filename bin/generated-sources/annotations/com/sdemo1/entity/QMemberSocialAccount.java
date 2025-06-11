package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberSocialAccount is a Querydsl query type for MemberSocialAccount
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberSocialAccount extends EntityPathBase<MemberSocialAccount> {

    private static final long serialVersionUID = -1149111967L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberSocialAccount memberSocialAccount = new QMemberSocialAccount("memberSocialAccount");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final QMember member;

    public final NumberPath<Integer> memberId = createNumber("memberId", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modifiedAt = createDateTime("modifiedAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> msaId = createNumber("msaId", Integer.class);

    public final StringPath profileImg = createString("profileImg");

    public final EnumPath<MemberSocialAccount.Provider> provider = createEnum("provider", MemberSocialAccount.Provider.class);

    public final StringPath providerId = createString("providerId");

    public final StringPath userLoginId = createString("userLoginId");

    public final StringPath userLoginPw = createString("userLoginPw");

    public QMemberSocialAccount(String variable) {
        this(MemberSocialAccount.class, forVariable(variable), INITS);
    }

    public QMemberSocialAccount(Path<? extends MemberSocialAccount> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberSocialAccount(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberSocialAccount(PathMetadata metadata, PathInits inits) {
        this(MemberSocialAccount.class, metadata, inits);
    }

    public QMemberSocialAccount(Class<? extends MemberSocialAccount> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

