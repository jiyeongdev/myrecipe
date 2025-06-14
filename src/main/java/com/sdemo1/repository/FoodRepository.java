package com.sdemo1.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sdemo1.entity.FoodItem;

@Repository
public interface FoodRepository extends JpaRepository<FoodItem , Integer> {
    List<FoodItem> getByParentID(String parentID);
    List<FoodItem> getAllByParentIDEquals(String parentID);
    List<FoodItem> findByParentIDIn(List<String> parentID);
    Page<FoodItem> findByParentID(String parentID, Pageable pageable);
    Page<FoodItem> findByFoodIDBetween(int start, int end, Pageable pageable);
    
}
