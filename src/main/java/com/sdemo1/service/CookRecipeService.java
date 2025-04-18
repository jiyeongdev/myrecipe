package com.sdemo1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sdemo1.dto.CookRecipeResponse;
import com.sdemo1.entity.CookItem;
import com.sdemo1.repository.CookItemRepository;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdemo1.dto.CookRecipeRequest;
import com.sdemo1.entity.RecipeStep;
import com.sdemo1.repository.RecipeStepRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CookRecipeService {

    @Autowired
    private CookItemRepository cookItemRepository;
    @Autowired
    private RecipeStepRepository recipeStepRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public CookRecipeService() {
    }

    public List<CookRecipeResponse> getRecipesByUserId(Integer userId) {
        List<CookItem> cookItems = cookItemRepository.findByUserId(userId);
        return cookItems.stream()
            .map(item -> {
                try {
                    RecipeStep recipeStep = recipeStepRepository.findByCookId(item.getCookId())
                        .orElse(null);
                    
                    return CookRecipeResponse.builder()
                        .cookId(item.getCookId())
                        .userId(item.getUserId())
                        .cookTitle(item.getCookTitle())
                        .cookImg(item.getCookImg())
                        .cookIngredient(objectMapper.readTree(item.getCookIngredient()))
                        .recipeSteps(recipeStep != null ? objectMapper.readTree(recipeStep.getRecipeSteps()) : null)
                        .build();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("JSON 파싱 중 오류 발생", e);
                }
            })
            .collect(Collectors.toList());
    }

    public List<CookRecipeResponse> getRecipesByCookId(Integer cookId) {
        List<CookItem> cookItems = cookItemRepository.findByCookId(cookId);
        return cookItems.stream()
            .map(item -> {
                try {
                    RecipeStep recipeStep = recipeStepRepository.findByCookId(item.getCookId())
                        .orElse(null);
                    
                    return CookRecipeResponse.builder()
                        .cookId(item.getCookId())
                        .userId(item.getUserId())
                        .cookTitle(item.getCookTitle())
                        .cookImg(item.getCookImg())
                        .cookIngredient(objectMapper.readTree(item.getCookIngredient()))
                        .recipeSteps(recipeStep != null ? objectMapper.readTree(recipeStep.getRecipeSteps()) : null)
                        .build();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("JSON 파싱 중 오류 발생", e);
                }
            })
            .collect(Collectors.toList());
    }
    @Transactional
    public Integer createRecipe(CookRecipeRequest request) throws Exception {
        CookItem cookItem = CookItem.builder()
            .userId(request.getUserId())
            .cookTitle(request.getCookTitle())
            .cookImg(request.getCookImg())
            .cookIngredient(objectMapper.writeValueAsString(request.getIngredients()))
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
            existingCookItem.setCookTitle(request.getCookTitle());
            existingCookItem.setCookImg(request.getCookImg());
            existingCookItem.setCookIngredient(objectMapper.writeValueAsString(request.getIngredients()));
            
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
}
