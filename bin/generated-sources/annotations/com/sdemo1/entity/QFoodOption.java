package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFoodOption is a Querydsl query type for FoodOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFoodOption extends EntityPathBase<FoodOption> {

    private static final long serialVersionUID = 774763576L;

    public static final QFoodOption foodOption = new QFoodOption("foodOption");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> optionId = createNumber("optionId", Integer.class);

    public final StringPath optionName = createString("optionName");

    public final NumberPath<Integer> optionType = createNumber("optionType", Integer.class);

    public QFoodOption(String variable) {
        super(FoodOption.class, forVariable(variable));
    }

    public QFoodOption(Path<? extends FoodOption> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFoodOption(PathMetadata metadata) {
        super(FoodOption.class, metadata);
    }

}

