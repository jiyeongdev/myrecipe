package com.sdemo1.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static com.sdemo1.common.utils.ValidateUtils.isNullOrEmpty;
import com.sdemo1.dto.FoodCategoryDto;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.exception.CustomException;
import com.sdemo1.repository.FoodQueryDSLRepository;
import com.sdemo1.repository.FoodRepository;
import com.sdemo1.util.FoodCategoryUtil;
import com.sdemo1.dto.PageRequestDto;

@Service
public class FoodService {

    @Autowired
    FoodRepository foodRepository;
    @Autowired
    FoodQueryDSLRepository foodQueryDSLRepository;

    public List<FoodItem> findByParentIDIn(List<String> parentID) {
        return foodRepository.findByParentIDIn(parentID);
    }

    public List<FoodItem> GetByParentID(String parentID) {
        return foodRepository.getByParentID(parentID);
    }

    public List<FoodItem> findFoodCategory() {
        return foodQueryDSLRepository.findFoodCategory();
    }

    public Map<String, Map<String, Object>> getCategoryMap() {
        List<FoodItem> foodItems = findFoodCategory();
        return FoodCategoryUtil.getCategoryMap(foodItems);
    }

    public Page<FoodItem> findIngredientByFilter(FoodCategoryDto params) {
        Map<String, Map<String, Object>> categoryMap = getCategoryMap();
        
        Page<FoodItem> item = null;
    
        // null 체크 추가
        if (isNullOrEmpty(params.getSID()) && isNullOrEmpty(params.getMID())) {
            item = findAllIngredients(params);
        } else if (isNullOrEmpty(params.getSID())) {
            item = findIngredientsByMainCategory(params);
        } else if (isNullOrEmpty(params.getMID())) {
            throw new CustomException("잘못된 요청입니다 (메인x,서브o)");
        } else {
            item = findIngredientsBySubCategory(params);
        }
    
        // null 체크 추가
        if (item == null) {
            throw new CustomException("조회된 데이터가 없습니다");
        }
    
        // categoryMap 정보를 item에 추가
        return item.map(foodItem -> {
            String parentID = foodItem.getParentID();
            Map<String, Object> categoryInfo = categoryMap.get(parentID);
            
            if (categoryInfo != null) {
                try {
                    foodItem.setSID(Integer.parseInt(parentID));
                    foodItem.setSName(String.valueOf(categoryInfo.get("sName")));
                    foodItem.setMID(Integer.parseInt(String.valueOf(categoryInfo.get("mID"))));
                    foodItem.setMName(String.valueOf(categoryInfo.get("mName")));
                } catch (Exception e) {
                    System.out.println("데이터 변환 중 오류: " + e.getMessage());
                }
            }
            return foodItem;
        });
    }

    private Page<FoodItem> findIngredientsByMainCategory(FoodCategoryDto params) {
        return foodQueryDSLRepository.findByMainCategoryWithPaging(
            params.getMID(), 
            params.getPageRequest().toPageable()
        );
    }

    private Page<FoodItem> findIngredientsBySubCategory(FoodCategoryDto params) {
        return foodRepository.findByParentID(params.getSID(), params.getPageRequest().toPageable());
    }

    private Page<FoodItem> findAllIngredients(FoodCategoryDto params) {
        return foodRepository.findByFoodIDBetween(50000, 59999, params.getPageRequest().toPageable());
    }

    public Page<FoodItem> findByFoodName(String keyword, PageRequestDto pageRequest) {
        Page<FoodItem> foodItems = foodQueryDSLRepository.findByFoodNameContainingAndFoodIdStartingWithFive(keyword, pageRequest.toPageable());
        
        Map<String, Map<String, Object>> categoryMap = getCategoryMap();
        // categoryMap 정보 item에 추가
        return foodItems.map(foodItem -> {
            String parentID = foodItem.getParentID();
            Map<String, Object> categoryInfo = categoryMap.get(parentID);
            
            if (categoryInfo != null) {
                try {
                    foodItem.setSID(Integer.parseInt(parentID));
                    foodItem.setSName(String.valueOf(categoryInfo.get("sName")));
                    foodItem.setMID(Integer.parseInt(String.valueOf(categoryInfo.get("mID"))));
                    foodItem.setMName(String.valueOf(categoryInfo.get("mName")));
                } catch (Exception e) {
                    System.out.println("데이터 변환 중 오류: " + e.getMessage());
                }
            }
            return foodItem;
        });
    }
}
