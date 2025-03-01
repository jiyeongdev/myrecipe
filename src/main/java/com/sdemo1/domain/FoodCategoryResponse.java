package com.sdemo1.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

// lombok
@Data
@AllArgsConstructor // 모든 필드를 포함하는 생성자(public RecipeStepDto(String description, File photoFile))를 자동 생성합니다.
public class FoodCategoryResponse{
    private List<FoodItem>  mainCategory;
    private Map<String , List<FoodItem>> subCategory;

}
