// package com.sdemo1.repository;

// import java.util.List;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Repository;

// import com.querydsl.jpa.impl.JPAQueryFactory;
// import com.sdemo1.entity.FoodItem;
// import static com.sdemo1.entity.QFoodItem.foodItem;

// @Repository
// public class FoodQueryDSLRepository {

//     private final JPAQueryFactory queryFactory;

//     public FoodQueryDSLRepository(JPAQueryFactory queryFactory) {
//         this.queryFactory = queryFactory;
//     }

//     public List<FoodItem> findFoodCategory() {
//         return queryFactory.selectFrom(foodItem)
//                 .where(foodItem.parentID.isNotNull()
//                         .and(foodItem.parentID.in("P", "R")))
//                 .fetch();
//     }

//     // 메인 카테고리에 대한 페이징 처리된 조회 메소드 추가
//     public Page<FoodItem> findByMainCategoryWithPaging(String mainCategoryId, Pageable pageable) {
//         var query = queryFactory
//             .selectFrom(foodItem)
//             .where(foodItem.parentID.in(
//                 JPAExpressions
//                     .select(foodItem.foodID.stringValue())
//                     .from(foodItem)
//                     .where(foodItem.parentID.eq(mainCategoryId))
//             ));

//         // 페이징이 없는 경우
//         if (pageable.isUnpaged()) {
//             var content = query.fetch();
//             return new PageImpl<>(content, pageable, content.size());
//         }

//         // 페이징이 있는 경우
//         var count = query.fetchCount();
//         var content = query
//             .offset(pageable.getOffset())
//             .limit(pageable.getPageSize())
//             .fetch();

//         return new PageImpl<>(content, pageable, count);
//     }
// }


//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sdemo1.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.entity.QFoodItem;

@Repository
public class FoodQueryDSLRepository {
    private final JPAQueryFactory queryFactory;

    public FoodQueryDSLRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<FoodItem> findFoodCategory() {
        return ((JPAQuery)this.queryFactory.selectFrom(QFoodItem.foodItem).where(QFoodItem.foodItem.parentID.in(new String[]{"P", "R"}).or(QFoodItem.foodItem.parentID.in((SubQueryExpression)JPAExpressions.select(QFoodItem.foodItem.foodID.stringValue()).from(new EntityPath[]{QFoodItem.foodItem}).where(new Predicate[]{QFoodItem.foodItem.parentID.in(new String[]{"P", "R"})}))))).fetch();
    }

    public Page<FoodItem> findByMainCategoryWithPaging(String mainCategoryId, Pageable pageable) {
        JPAQuery<FoodItem> query = (JPAQuery)this.queryFactory.selectFrom(QFoodItem.foodItem).where(QFoodItem.foodItem.parentID.in((SubQueryExpression)JPAExpressions.select(QFoodItem.foodItem.foodID.stringValue()).from(new EntityPath[]{QFoodItem.foodItem}).where(new Predicate[]{QFoodItem.foodItem.parentID.eq(mainCategoryId)})));
        List content;
        if (pageable.isUnpaged()) {
            content = query.fetch();
            return new PageImpl(content, pageable, (long)content.size());
        } else {
            content = ((JPAQuery)((JPAQuery)query.offset(pageable.getOffset())).limit((long)pageable.getPageSize())).fetch();
            Long count = (Long)((JPAQuery)((JPAQuery)this.queryFactory.select(QFoodItem.foodItem.count()).from(QFoodItem.foodItem)).where(QFoodItem.foodItem.parentID.in((SubQueryExpression)JPAExpressions.select(QFoodItem.foodItem.foodID.stringValue()).from(new EntityPath[]{QFoodItem.foodItem}).where(new Predicate[]{QFoodItem.foodItem.parentID.eq(mainCategoryId)})))).fetchOne();
            return new PageImpl(content, pageable, count);
        }
    }
    
    /**
     * 음식 이름에 키워드가 포함되어 있고, parent_id가 '5'로 시작하는 두글자 이상인 FoodItem을 검색합니다.
     * 
     * @param keyword 검색 키워드
     * @return 조건에 맞는 FoodItem 목록
     */
    public List<FoodItem> findByFoodNameContainingAndFoodIdStartingWithFive(String keyword) {
        return queryFactory
            .selectFrom(QFoodItem.foodItem)
            .where(
                QFoodItem.foodItem.foodName.contains(keyword)
                .and(QFoodItem.foodItem.foodID.stringValue().startsWith("5"))
                .and(QFoodItem.foodItem.foodID.stringValue().length().goe(2))
            )
            .fetch();
    }
}
