package com.sdemo1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sdemo1.entity.CookItem;

@Repository
public interface CookItemRepository extends JpaRepository<CookItem, Integer> {
    List<CookItem> findByUserId(Integer userId);
    List<CookItem> findByCookId(Integer cookId);
    List<CookItem> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<CookItem> findByCookIdIn(List<Integer> cookIds);
    
    /**
     * DB 레벨 최적화: 재료 매칭률 기준 상위 레시피 조회
     * MySQL JSON 함수를 활용하여 매칭률 계산 및 정렬을 DB에서 수행
     */
    @Query(value = """
        SELECT c.*, 
               COALESCE(matching_count.match_count, 0) as matching_count
        FROM cook_item c
        LEFT JOIN (
            SELECT cook_id, COUNT(*) as match_count
            FROM (
                SELECT c2.cook_id,
                       JSON_UNQUOTE(JSON_EXTRACT(ingredient.value, '$.foodName')) as ingredient_name
                FROM cook_item c2,
                     JSON_TABLE(c2.cook_ingredient, '$[*]' COLUMNS (
                         value JSON PATH '$'
                     )) as ingredient
                WHERE c2.user_id != :memberId
                  AND JSON_UNQUOTE(JSON_EXTRACT(ingredient.value, '$.foodName')) IN (:ingredients)
            ) matched_ingredients
            GROUP BY cook_id
        ) matching_count ON c.cook_id = matching_count.cook_id
        WHERE c.user_id != :memberId
          AND matching_count.match_count > 0
        ORDER BY matching_count.match_count DESC, c.created_at DESC
        LIMIT :limitCount
        """, nativeQuery = true)
    List<CookItem> findTopMatchingRecipesByIngredients(
        @Param("ingredients") List<String> ingredients,
        @Param("memberId") Integer memberId,
        @Param("limitCount") int limitCount
    );
    
    /**
     * 빠른 매칭: JSON_OVERLAPS 함수로 재료 포함 여부만 확인
     * 상세 매칭률 계산 없이 빠르게 후보군 선별
     */
    @Query(value = """
        SELECT c.*
        FROM cook_item c
        WHERE c.user_id != :memberId
          AND JSON_OVERLAPS(
              c.cook_ingredient,
              CAST(:ingredientsJson AS JSON)
          )
        ORDER BY c.created_at DESC
        LIMIT :limitCount
        """, nativeQuery = true)
    List<CookItem> findRecipesWithAnyMatchingIngredient(
        @Param("ingredientsJson") String ingredientsJson,
        @Param("memberId") Integer memberId,
        @Param("limitCount") int limitCount
    );
    
    /**
     * 하이브리드 조회: 높은 매칭률과 다양성을 모두 고려
     * 1단계: 높은 매칭률 우선 조회
     */
    @Query(value = """
        SELECT c.*
        FROM cook_item c
        WHERE c.user_id != :memberId
          AND (
              SELECT COUNT(*)
              FROM JSON_TABLE(c.cook_ingredient, '$[*]' COLUMNS (
                  ingredient_name VARCHAR(255) PATH '$.foodName'
              )) jt
              WHERE jt.ingredient_name IN (:ingredients)
          ) >= :minMatchCount
        ORDER BY (
            SELECT COUNT(*)
            FROM JSON_TABLE(c.cook_ingredient, '$[*]' COLUMNS (
                ingredient_name VARCHAR(255) PATH '$.foodName'
            )) jt
            WHERE jt.ingredient_name IN (:ingredients)
        ) DESC, c.created_at DESC
        LIMIT :limitCount
        """, nativeQuery = true)
    List<CookItem> findHighMatchingRecipes(
        @Param("ingredients") List<String> ingredients,
        @Param("memberId") Integer memberId,
        @Param("minMatchCount") int minMatchCount,
        @Param("limitCount") int limitCount
    );
}
