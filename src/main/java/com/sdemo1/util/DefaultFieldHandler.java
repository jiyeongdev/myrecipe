package com.sdemo1.util;

import com.sdemo1.request.CookRecipeRequest;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

/**
 * 객체의 필드에 대한 기본값을 처리하는 핸들러
 * 
 * 이 클래스는 다음과 같은 기능을 제공합니다:
 * 1. 객체의 필드가 누락된 경우 기본값 설정
 * 2. 배열과 단일 객체 모두 처리 가능
 * 3. 에러 발생 시 기본 구조 반환
 * 
 * 사용 예시:
 * - Ingredient 객체의 필드 처리: setDefaultIngredientFields()
 * - RecipeSteps 객체의 필드 처리: setDefaultRecipeStepFields()
 */
@Component
public class DefaultFieldHandler {


    /**
     * Ingredient 객체의 필드를 처리하여 누락된 경우 기본값 설정
     */
    public List<CookRecipeRequest.Ingredient> setDefaultIngredientFields(List<CookRecipeRequest.Ingredient> ingredients) {
        try {
            if (ingredients == null) {
                return new ArrayList<>();
            }
            
            List<CookRecipeRequest.Ingredient> processedIngredients = new ArrayList<>();
            for (CookRecipeRequest.Ingredient ingredient : ingredients) {
                processedIngredients.add(processIngredient(ingredient));
            }
            return processedIngredients;
        } catch (Exception e) {
            // 오류 로그 남기기
            System.err.println("재료 처리 중 오류 발생: " + e.getMessage());
            
            // 기본 객체 생성 및 반환
            List<CookRecipeRequest.Ingredient> errorList = new ArrayList<>();
            CookRecipeRequest.Ingredient errorIngredient = createDefaultIngredient();
            errorIngredient.setFoodName("오류: " + e.getMessage());
            errorList.add(errorIngredient);
            return errorList;
        }
    }

    private CookRecipeRequest.Ingredient processIngredient(CookRecipeRequest.Ingredient ingredient) {
        CookRecipeRequest.Ingredient processed = new CookRecipeRequest.Ingredient();
        processed.setUnit(ingredient.getUnit() != null ? ingredient.getUnit() : "");
        processed.setFoodId(ingredient.getFoodId() != null ? ingredient.getFoodId() : 0);
        processed.setUnitId(ingredient.getUnitId() != null ? ingredient.getUnitId() : 0);
        processed.setQuantity(ingredient.getQuantity() != null ? ingredient.getQuantity() : "0");
        processed.setFoodName(ingredient.getFoodName() != null ? ingredient.getFoodName() : "알 수 없음");
        processed.setIsRequired(ingredient.getIsRequired() != null ? ingredient.getIsRequired() : "Y");
        return processed;
    }

    private CookRecipeRequest.Ingredient createDefaultIngredient() {
        CookRecipeRequest.Ingredient defaultIngredient = new CookRecipeRequest.Ingredient();
        defaultIngredient.setUnit("");
        defaultIngredient.setFoodId(0);
        defaultIngredient.setUnitId(0);
        defaultIngredient.setQuantity("0");
        defaultIngredient.setFoodName("알 수 없음");
        defaultIngredient.setIsRequired("Y");
        return defaultIngredient;
    }

    /**
     * RecipeSteps 객체의 필드를 처리하여 누락된 경우 기본값 설정
     */
    public List<CookRecipeRequest.RecipeStepDetail> setDefaultRecipeStepFields(List<CookRecipeRequest.RecipeStepDetail> recipeSteps) {
        try {
            if (recipeSteps == null) {
                return new ArrayList<>();
            }
            
            List<CookRecipeRequest.RecipeStepDetail> processedSteps = new ArrayList<>();
            for (CookRecipeRequest.RecipeStepDetail step : recipeSteps) {
                processedSteps.add(processRecipeStep(step));
            }
            return processedSteps;
        } catch (Exception e) {
            // 오류 로그 남기기
            System.err.println("레시피 단계 처리 중 오류 발생: " + e.getMessage());
            
            // 기본 객체 생성 및 반환
            List<CookRecipeRequest.RecipeStepDetail> errorList = new ArrayList<>();
            CookRecipeRequest.RecipeStepDetail errorStep = createDefaultRecipeStep();
            errorStep.setDescription("오류: " + e.getMessage());
            errorList.add(errorStep);
            return errorList;
        }
    }

    private CookRecipeRequest.RecipeStepDetail processRecipeStep(CookRecipeRequest.RecipeStepDetail step) {
        CookRecipeRequest.RecipeStepDetail processed = new CookRecipeRequest.RecipeStepDetail();
        processed.setDescription(step.getDescription() != null ? step.getDescription() : "");
        processed.setImg(step.getImg() != null ? step.getImg() : "");
        return processed;
    }

    private CookRecipeRequest.RecipeStepDetail createDefaultRecipeStep() {
        CookRecipeRequest.RecipeStepDetail defaultStep = new CookRecipeRequest.RecipeStepDetail();
        defaultStep.setDescription("");
        defaultStep.setImg("");
        return defaultStep;
    }

    /**
     * Ingredient 객체의 필드가 모두 존재하는지 검증
     */
    public void validateIngredientFields(List<CookRecipeRequest.Ingredient> ingredients) {
        if (ingredients == null) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다: ingredients");
        }
        
        List<String> missingFields = new ArrayList<>();
        for (CookRecipeRequest.Ingredient ingredient : ingredients) {
            validateIngredient(ingredient, missingFields);
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다: " + String.join(", ", missingFields));
        }
    }
    
    /**
     * RecipeSteps 객체의 필드가 모두 존재하는지 검증
     */
    public void validateRecipeStepFields(List<CookRecipeRequest.RecipeStepDetail> recipeSteps) {
        if (recipeSteps == null) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다: recipeSteps");
        }
        
        List<String> missingFields = new ArrayList<>();
        for (CookRecipeRequest.RecipeStepDetail recipeStep : recipeSteps) {
            validateRecipeStep(recipeStep, missingFields);
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다: " + String.join(", ", missingFields));
        }
    }
    
    private void validateIngredient(CookRecipeRequest.Ingredient ingredient, List<String> missingFields) {
        if (ingredient.getUnit() == null) missingFields.add("unit");
        if (ingredient.getFoodId() == null) missingFields.add("foodId");
        if (ingredient.getUnitId() == null) missingFields.add("unitId");
        if (ingredient.getQuantity() == null) missingFields.add("quantity");
        if (ingredient.getFoodName() == null) missingFields.add("foodName");
        if (ingredient.getIsRequired() == null) missingFields.add("isRequired");
    }
    
    private void validateRecipeStep(CookRecipeRequest.RecipeStepDetail recipeStep, List<String> missingFields) {
        if (recipeStep.getDescription() == null) missingFields.add("description");
        if (recipeStep.getImg() == null) missingFields.add("img");
    }
  
} 