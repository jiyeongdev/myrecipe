package com.sdemo1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sdemo1.entity.CookItem;

@Repository
public interface CookItemRepository extends JpaRepository<CookItem, Integer> {
    List<CookItem> findByUserId(Integer userId);
    List<CookItem> findByCookId(Integer cookId);
}
