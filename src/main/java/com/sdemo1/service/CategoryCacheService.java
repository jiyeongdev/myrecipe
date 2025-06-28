package com.sdemo1.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sdemo1.entity.FoodItem;
import com.sdemo1.repository.FoodQueryDSLRepository;
import com.sdemo1.util.FoodCategoryUtil;

@Service
public class CategoryService {

    @Autowired
    private FoodQueryDSLRepository foodQueryDSLRepository;

    // 카테고리 맵 캐싱 - 완전 정적 데이터이므로 7일 캐싱
    private Map<String, Map<String, Object>> cachedCategoryMap;
    private long lastCacheUpdate = 0;
    private static final long CACHE_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일

    /**
     * 카테고리 맵 조회 (캐시 적용)
     */
    public Map<String, Map<String, Object>> getCategoryMap() {
        long currentTime = System.currentTimeMillis();
        
        // 캐시가 없거나 만료된 경우에만 새로 조회
        if (cachedCategoryMap == null || (currentTime - lastCacheUpdate) > CACHE_EXPIRY_TIME) {
            refreshCategoryMap();
        }
        
        return cachedCategoryMap;
    }

    /**
     * 카테고리 맵 수동 갱신 (관리자용)
     */
    public void refreshCategoryMap() {
        List<FoodItem> foodItems = findFoodCategory();
        cachedCategoryMap = FoodCategoryUtil.getCategoryMap(foodItems);
        lastCacheUpdate = System.currentTimeMillis();
    }

    /**
     * 음식 카테고리 조회
     */
    private List<FoodItem> findFoodCategory() {
        return foodQueryDSLRepository.findFoodCategory();
    }

    /**
     * 캐시 상태 확인 (디버깅용)
     */
    public String getCacheStatus() {
        if (cachedCategoryMap == null) {
            return "캐시 없음";
        }
        
        long cacheAge = System.currentTimeMillis() - lastCacheUpdate;
        long remainingTime = CACHE_EXPIRY_TIME - cacheAge;
        
        return String.format("캐시 크기: %d, 남은 시간: %d분", 
                           cachedCategoryMap.size(), 
                           remainingTime / (60 * 1000));
    }
} 