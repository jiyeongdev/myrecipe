package com.sdemo1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sdemo1.entity.CookItem;

@Repository
public interface CookRecipeRepository extends JpaRepository<CookItem, Integer> {
    List<CookItem> findByUserId(Integer userId);
}
