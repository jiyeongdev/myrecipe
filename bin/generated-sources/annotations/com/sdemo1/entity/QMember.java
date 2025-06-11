package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 1887561439L;

    public static final QMember member = new QMember("member1");

    public final BooleanPath completeFlag = createBoolean("completeFlag");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> memberId = createNumber("memberId", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modifiedAt = createDateTime("modifiedAt", java.sql.Timestamp.class);

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final EnumPath<Member.Role> role = createEnum("role", Member.Role.class);

    public final ListPath<MemberSocialAccount, QMemberSocialAccount> socialAccounts = this.<MemberSocialAccount, QMemberSocialAccount>createList("socialAccounts", MemberSocialAccount.class, QMemberSocialAccount.class, PathInits.DIRECT2);

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

