package com.sdemo1.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sdemo1.entity.FoodItem;
import static com.sdemo1.entity.QFoodItem.foodItem;

@Repository
public class FoodQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    public FoodQueryDSLRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<FoodItem> findFoodCategory() {
        return queryFactory.selectFrom(foodItem)
                .where(foodItem.parentID.in("P", "R")
                        .or(foodItem.parentID.in(
                                JPAExpressions.select(foodItem.foodID.stringValue())
                                        .from(foodItem)
                                        .where(foodItem.parentID.in("P", "R"))
                        ))
                ).fetch();
    }

    // 메인 카테고리에 대한 페이징 처리된 조회 메소드 추가
    public Page<FoodItem> findByMainCategoryWithPaging(String mainCategoryId, Pageable pageable) {
        var query = queryFactory
            .selectFrom(foodItem)
            .where(foodItem.parentID.in(
                JPAExpressions
                    .select(foodItem.foodID.stringValue())
                    .from(foodItem)
                    .where(foodItem.parentID.eq(mainCategoryId))
            ));

        // 페이징이 없는 경우
        if (pageable.isUnpaged()) {
            var content = query.fetch();
            return new PageImpl<>(content, pageable, content.size());
        }

        // 페이징이 있는 경우
        var content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        var count = queryFactory
            .select(foodItem.count())
            .from(foodItem)
            .where(foodItem.parentID.in(
                JPAExpressions
                    .select(foodItem.foodID.stringValue())
                    .from(foodItem)
                    .where(foodItem.parentID.eq(mainCategoryId))
            ))
            .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }
}
