package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFoodItem is a Querydsl query type for FoodItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFoodItem extends EntityPathBase<FoodItem> {

    private static final long serialVersionUID = -1796015210L;

    public static final QFoodItem foodItem = new QFoodItem("foodItem");

    public final NumberPath<Integer> foodID = createNumber("foodID", Integer.class);

    public final StringPath foodImg = createString("foodImg");

    public final StringPath foodName = createString("foodName");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath parentID = createString("parentID");

    public QFoodItem(String variable) {
        super(FoodItem.class, forVariable(variable));
    }

    public QFoodItem(Path<? extends FoodItem> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFoodItem(PathMetadata metadata) {
        super(FoodItem.class, metadata);
    }

}

