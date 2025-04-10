package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRecipeStep is a Querydsl query type for RecipeStep
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeStep extends EntityPathBase<RecipeStep> {

    private static final long serialVersionUID = 948570943L;

    public static final QRecipeStep recipeStep = new QRecipeStep("recipeStep");

    public final NumberPath<Integer> cookId = createNumber("cookId", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> recipeId = createNumber("recipeId", Integer.class);

    public final StringPath recipeSteps = createString("recipeSteps");

    public QRecipeStep(String variable) {
        super(RecipeStep.class, forVariable(variable));
    }

    public QRecipeStep(Path<? extends RecipeStep> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRecipeStep(PathMetadata metadata) {
        super(RecipeStep.class, metadata);
    }

}

