package com.sdemo1.repository;

import com.sdemo1.entity.FoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FoodIngredientRepository extends JpaRepository<FoodIngredient, Integer> {
    
    List<FoodIngredient> findByMemberIdOrderByCreatedAtDesc(Integer memberId);
    
    @Modifying
    @Transactional
    @Query(value = """
        INSERT IGNORE INTO food_ingredient (member_id, food_id, food_name, created_at, modified_at) 
        VALUES (:memberId, :foodId, :foodName, NOW(), NOW())
        """, nativeQuery = true)
    int insertIgnoreIngredient(@Param("memberId") Integer memberId, 
                              @Param("foodId") Integer foodId, 
                              @Param("foodName") String foodName);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM FoodIngredient f WHERE f.memberId = :memberId AND f.foodId IN :foodIds")
    int deleteByMemberIdAndFoodIdIn(@Param("memberId") Integer memberId, 
                                   @Param("foodIds") List<Integer> foodIds);
} 