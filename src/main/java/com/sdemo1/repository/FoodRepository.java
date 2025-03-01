package com.sdemo1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sdemo1.entity.FoodItem;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<FoodItem , Integer> {

    List<FoodItem> findByParentIDIn(List<String> parentID);

    List<FoodItem> getByParentID(String parentID);

    List<FoodItem> getAllByParentIDEquals(String parentID);

}
