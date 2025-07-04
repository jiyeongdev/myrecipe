package com.sdemo1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sdemo1.response.CookRecipeResponse;
import com.sdemo1.entity.CookItem;
import com.sdemo1.repository.CookItemRepository;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdemo1.request.CookRecipeRequest;
import com.sdemo1.entity.RecipeStep;
import com.sdemo1.repository.RecipeStepRepository;
import org.springframework.transaction.annotation.Transactional;
import com.sdemo1.util.DefaultFieldHandler;

@Service
public class CookRecipeService {

    @Autowired
    private CookItemRepository cookItemRepository;
    @Autowired
    private RecipeStepRepository recipeStepRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DefaultFieldHandler defaultFieldHandler;

    public CookRecipeService() {
    }

    public List<CookRecipeResponse> getRecipesByUserId(Integer userId) {
        List<CookItem> cookItems = cookItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return cookItems.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public List<CookRecipeResponse> getRecipesByCookId(Integer cookId) {
        List<CookItem> cookItems = cookItemRepository.findByCookId(cookId);
        return cookItems.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * CookItem을 CookRecipeResponse로 변환
     */
    private CookRecipeResponse convertToResponse(CookItem item) {
        try {
            RecipeStep recipeStep = recipeStepRepository.findByCookId(item.getCookId())
                .orElse(null);
            
            // cookIngredient 파싱 및 필드 누락 처리
            List<CookRecipeRequest.Ingredient> ingredients = objectMapper.readValue(
                item.getIngredients(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CookRecipeRequest.Ingredient.class)
            );
            List<CookRecipeRequest.Ingredient> processedIngredients = defaultFieldHandler.setDefaultIngredientFields(ingredients);
            
            // recipeSteps 파싱 및 필드 누락 처리
            List<CookRecipeRequest.RecipeStepDetail> recipeSteps = null;
            if (recipeStep != null) {
                recipeSteps = objectMapper.readValue(
                    recipeStep.getRecipeSteps(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CookRecipeRequest.RecipeStepDetail.class)
                );
                recipeSteps = defaultFieldHandler.setDefaultRecipeStepFields(recipeSteps);
            }
            
            return CookRecipeResponse.builder()
                .cookId(item.getCookId())
                .userId(item.getUserId())
                .cookTitle(item.getCookTitle())
                .cookImg(item.getCookImg())
                .Ingredients(processedIngredients)
                .recipeSteps(recipeSteps)
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 중 오류 발생", e);
        }
    }

    @Transactional
    public Integer createRecipe(CookRecipeRequest request) throws Exception {
        CookItem cookItem = CookItem.builder()
            .userId(request.getUserId())
            .cookTitle(request.getCookTitle())
            .cookImg(request.getCookImg())
            .ingredients(objectMapper.writeValueAsString(request.getIngredients()))
            .build();
        
        cookItem = cookItemRepository.save(cookItem);
        
        RecipeStep recipeStep = RecipeStep.builder()
            .cookId(cookItem.getCookId())
            .recipeSteps(objectMapper.writeValueAsString(request.getRecipeSteps()))
            .build();
        
        recipeStepRepository.save(recipeStep);
        return cookItem.getCookId();
    }
    
    @Transactional
    public void updateRecipe(int cookId, CookRecipeRequest request) throws Exception {
        // CookItem 업데이트
        CookItem existingCookItem = cookItemRepository.findById(cookId)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 레시피가 없습니다."));
        
        try {
            // 필드 검증
            defaultFieldHandler.validateIngredientFields(request.getIngredients());
            defaultFieldHandler.validateRecipeStepFields(request.getRecipeSteps());
            
            existingCookItem.setCookTitle(request.getCookTitle());
            existingCookItem.setCookImg(request.getCookImg());
            existingCookItem.setIngredients(objectMapper.writeValueAsString(request.getIngredients()));
            
            // RecipeStep 업데이트
            recipeStepRepository.findByCookId(cookId)
                .ifPresentOrElse(
                    existingRecipeStep -> {
                        try {
                            existingRecipeStep.setRecipeSteps(objectMapper.writeValueAsString(request.getRecipeSteps()));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("레시피 단계 업데이트 중 오류가 발생했습니다.", e);
                        }
                    },
                    () -> {
                        try {
                            RecipeStep recipeStep = RecipeStep.builder()
                                .cookId(cookId)
                                .recipeSteps(objectMapper.writeValueAsString(request.getRecipeSteps()))
                                .build();
                            recipeStepRepository.save(recipeStep);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("레시피 단계 생성 중 오류가 발생했습니다.", e);
                        }
                    }
                );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("레시피 업데이트 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 레시피 삭제
     * @param cookId 삭제할 레시피 ID
     * @throws IllegalArgumentException 레시피가 존재하지 않는 경우
     */
    @Transactional
    public void deleteRecipe(int cookId) {
        // CookItem 존재 여부 확인
        CookItem existingCookItem = cookItemRepository.findById(cookId)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 레시피가 없습니다."));
        
        // RecipeStep 삭제
        recipeStepRepository.findByCookId(cookId)
            .ifPresent(recipeStep -> recipeStepRepository.delete(recipeStep));
        
        // CookItem 삭제
        cookItemRepository.delete(existingCookItem);
    }
}
