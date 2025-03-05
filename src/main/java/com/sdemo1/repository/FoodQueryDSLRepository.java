package com.sdemo1.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.sdemo1.common.utils.ValidateUtils.isNullOrEmpty;
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

    //이제 필요없어진 듯
    public List<FoodItem> findIngredientByID(Map<String, String> cIDs) {
        JPAQuery<FoodItem> query = queryFactory.selectFrom(foodItem);

        String mID = cIDs.get("mID");
        String sID = cIDs.get("sID");

        // 특정 서브카테고리(sID)에 해당되는 음식 전부 요청
        if (!isNullOrEmpty(sID)) {
            query.where(foodItem.parentID.eq(sID));
        } 
        // 메인 카테고리에 해당되는 음식 전부 요청
        else if (!isNullOrEmpty(mID)) {
            query.where(foodItem.parentID.in(
                    JPAExpressions.select(foodItem.foodID.stringValue())
                            .from(foodItem)
                            .where(foodItem.parentID.eq(mID))
            ));
        } else { // mID, sID 키 자체가 존재하지 않으면 전체 음식 요청
            throw new IllegalArgumentException("mID와 sID 키 자체가 존재하지 않습니다.");
        }

        return query.fetch();
    }

    // 메인 카테고리에 대한 페이징 처리된 조회 메소드 추가
    public Page<FoodItem> findByMainCategoryWithPaging(String mainCategoryId, Pageable pageable) {
        var content = queryFactory
            .selectFrom(foodItem)
            .where(foodItem.parentID.in(
                JPAExpressions
                    .select(foodItem.foodID.stringValue())
                    .from(foodItem)
                    .where(foodItem.parentID.eq(mainCategoryId))
            ))
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
