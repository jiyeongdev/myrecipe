package com.sdemo1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static com.sdemo1.common.utils.ValidateUtils.isNullOrEmpty;
import com.sdemo1.dto.FoodCategoryDto;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.exception.CustomException;
import com.sdemo1.repository.FoodQueryDSLRepository;
import com.sdemo1.repository.FoodRepository;
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

    

    public Page<FoodItem> findIngredientByFilter(FoodCategoryDto params) {

        if (isNullOrEmpty(params.getSID()) && isNullOrEmpty(params.getMID())) {
            return findAllIngredients(params);
        } else if (isNullOrEmpty(params.getSID())) {
            return findIngredientsByMainCategory(params);
        } else if (isNullOrEmpty(params.getMID())) {
            throw new CustomException("잘못된 요청입니다 (메인x,서브o)");
        } else {
            return findIngredientsBySubCategory(params);
        }
    }

    private Page<FoodItem> findIngredientsByMainCategory(FoodCategoryDto params) {
        return foodQueryDSLRepository.findByMainCategoryWithPaging(
            params.getMID(), 
            params.getPageRequest().toPageable()
        );
    }

    private Page<FoodItem> findIngredientsBySubCategory(FoodCategoryDto params) { //ok
        return foodRepository.findByParentID(params.getSID(), params.getPageRequest().toPageable());
    }

    private Page<FoodItem> findAllIngredients(FoodCategoryDto params) {
        return foodRepository.findAll(params.getPageRequest().toPageable());
    }
}
