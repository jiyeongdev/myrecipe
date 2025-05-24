package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCookItem is a Querydsl query type for CookItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCookItem extends EntityPathBase<CookItem> {

    private static final long serialVersionUID = 1571953024L;

    public static final QCookItem cookItem = new QCookItem("cookItem");

    public final NumberPath<Integer> cookId = createNumber("cookId", Integer.class);

    public final StringPath cookImg = createString("cookImg");

    public final StringPath cookTitle = createString("cookTitle");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath ingredients = createString("ingredients");

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QCookItem(String variable) {
        super(CookItem.class, forVariable(variable));
    }

    public QCookItem(Path<? extends CookItem> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCookItem(PathMetadata metadata) {
        super(CookItem.class, metadata);
    }

}

