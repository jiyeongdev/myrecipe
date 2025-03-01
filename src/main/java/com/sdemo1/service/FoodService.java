package com.sdemo1.service;

import com.sdemo1.domain.FoodCategoryDto;
import com.sdemo1.domain.PageRequestDTO;
import com.sdemo1.domain.FoodItem;
import com.sdemo1.repository.FoodQueryDSLRepository;
import com.sdemo1.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.sdemo1.common.utils.ValidateUtils.isNullOrEmpty;

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

    public ResponseEntity<?> findIngredientByID(Map<String, String> CIDs) {
        return foodQueryDSLRepository.findIngredientByID(CIDs);
    }

    public ResponseEntity<?> allIngredient(Integer lastID, int size) {
        return null;
    }

    public ResponseEntity<?> findIngredientByFilter(Map<String, String> params) {
        FoodCategoryDto filter = FoodCategoryDto.from(params);
        PageRequestDTO pageRequest = PageRequestDTO.getDefault();

        if (isNullOrEmpty(filter.getSID()) && isNullOrEmpty(filter.getMID())) {
            return ResponseEntity.ok(findAllIngredients(pageRequest));
        } else if (isNullOrEmpty(filter.getSID())) {
            return ResponseEntity.ok(findIngredientsByMainCategory(filter.getMID()));
        } else if (isNullOrEmpty(filter.getMID())) {
            throw new IllegalArgumentException("잘못된 요청입니다 (메인x,서브o)");
        } else {
            return ResponseEntity.ok(findIngredientsBySubCategory(filter.getSID()));
        }
    }

    private List<FoodItem> findAllIngredients(PageRequestDTO pageRequest) {
        return foodRepository.findAll(pageRequest.toPageable()).getContent();
    }

    private List<FoodItem> findIngredientsByMainCategory(String mainCategoryId) {
        return foodRepository.getByParentID(mainCategoryId);
    }

    private List<FoodItem> findIngredientsBySubCategory(String subCategoryId) {
        return foodRepository.getAllByParentIDEquals(subCategoryId);
    }
}
