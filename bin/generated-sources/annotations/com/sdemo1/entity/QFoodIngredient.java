package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFoodIngredient is a Querydsl query type for FoodIngredient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFoodIngredient extends EntityPathBase<FoodIngredient> {

    private static final long serialVersionUID = 1663321108L;

    public static final QFoodIngredient foodIngredient = new QFoodIngredient("foodIngredient");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> fInId = createNumber("fInId", Integer.class);

    public final NumberPath<Integer> foodId = createNumber("foodId", Integer.class);

    public final StringPath foodName = createString("foodName");

    public final NumberPath<Integer> memberId = createNumber("memberId", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt", java.time.LocalDateTime.class);

    public QFoodIngredient(String variable) {
        super(FoodIngredient.class, forVariable(variable));
    }

    public QFoodIngredient(Path<? extends FoodIngredient> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFoodIngredient(PathMetadata metadata) {
        super(FoodIngredient.class, metadata);
    }

}

