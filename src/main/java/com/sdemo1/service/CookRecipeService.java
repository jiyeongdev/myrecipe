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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


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
                    
                    // cookIngredient 파싱 및 필드 누락 처리
                    JsonNode ingredientNode = objectMapper.readTree(item.getCookIngredient());
                    JsonNode processedIngredientNode = processIngredientNode(ingredientNode);
                    
                    // recipeSteps 파싱 및 필드 누락 처리
                    JsonNode recipeStepsNode = null;
                    if (recipeStep != null) {
                        recipeStepsNode = objectMapper.readTree(recipeStep.getRecipeSteps());
                        recipeStepsNode = processRecipeStepsNode(recipeStepsNode);
                    }
                    
                    return CookRecipeResponse.builder()
                        .cookId(item.getCookId())
                        .userId(item.getUserId())
                        .cookTitle(item.getCookTitle())
                        .cookImg(item.getCookImg())
                        .cookIngredient(processedIngredientNode)
                        .recipeSteps(recipeStepsNode)
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
                    
                    // cookIngredient 파싱 및 필드 누락 처리
                    JsonNode ingredientNode = objectMapper.readTree(item.getCookIngredient());
                    JsonNode processedIngredientNode = processIngredientNode(ingredientNode);
                    
                    // recipeSteps 파싱 및 필드 누락 처리
                    JsonNode recipeStepsNode = null;
                    if (recipeStep != null) {
                        recipeStepsNode = objectMapper.readTree(recipeStep.getRecipeSteps());
                        recipeStepsNode = processRecipeStepsNode(recipeStepsNode);
                    }
                    
                    return CookRecipeResponse.builder()
                        .cookId(item.getCookId())
                        .userId(item.getUserId())
                        .cookTitle(item.getCookTitle())
                        .cookImg(item.getCookImg())
                        .cookIngredient(processedIngredientNode)
                        .recipeSteps(recipeStepsNode)
                        .build();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("JSON 파싱 중 오류 발생", e);
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Ingredient 노드를 처리하여 필드 누락 시 기본값 설정
     */
    private JsonNode processIngredientNode(JsonNode ingredientNode) {
        try {
            // 배열인 경우 각 요소 처리
            if (ingredientNode.isArray()) {
                ArrayNode resultArray = objectMapper.createArrayNode();
                
                for (JsonNode node : ingredientNode) {
                    ObjectNode processedNode = objectMapper.createObjectNode();
                    
                    // 필수 필드 확인 및 기본값 설정
                    processedNode.put("unit", node.has("unit") ? node.get("unit").asText() : "");
                    processedNode.put("foodId", node.has("foodId") ? node.get("foodId").asInt() : 0);
                    processedNode.put("unitId", node.has("unitId") ? node.get("unitId").asInt() : 0);
                    processedNode.put("quantity", node.has("quantity") ? node.get("quantity").asText() : "0");
                    processedNode.put("foodName", node.has("foodName") ? node.get("foodName").asText() : "알 수 없음");
                    processedNode.put("isRequired", node.has("isRequired") ? node.get("isRequired").asText() : "Y");
                    
                    resultArray.add(processedNode);
                }
                
                return resultArray;
            } 
            // 단일 객체인 경우
            else if (ingredientNode.isObject()) {
                ObjectNode processedNode = objectMapper.createObjectNode();
                
                // 필수 필드 확인 및 기본값 설정
                processedNode.put("unit", ingredientNode.has("unit") ? ingredientNode.get("unit").asText() : "");
                processedNode.put("foodId", ingredientNode.has("foodId") ? ingredientNode.get("foodId").asInt() : 0);
                processedNode.put("unitId", ingredientNode.has("unitId") ? ingredientNode.get("unitId").asInt() : 0);
                processedNode.put("quantity", ingredientNode.has("quantity") ? ingredientNode.get("quantity").asText() : "0");
                processedNode.put("foodName", ingredientNode.has("foodName") ? ingredientNode.get("foodName").asText() : "알 수 없음");
                processedNode.put("isRequired", ingredientNode.has("isRequired") ? ingredientNode.get("isRequired").asText() : "Y");
                
                return processedNode;
            }
            
            // 배열이나 객체가 아닌 경우 기본 구조 반환
            ObjectNode defaultNode = objectMapper.createObjectNode();
            defaultNode.put("unit", "");
            defaultNode.put("foodId", 0);
            defaultNode.put("unitId", 0);
            defaultNode.put("quantity", "0");
            defaultNode.put("foodName", "알 수 없음");
            defaultNode.put("isRequired", "Y");
            
            return defaultNode;
        } catch (Exception e) {
            // 오류 발생 시 기본 구조 반환
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("unit", "");
            errorNode.put("foodId", 0);
            errorNode.put("unitId", 0);
            errorNode.put("quantity", "0");
            errorNode.put("foodName", "알 수 없음");
            errorNode.put("isRequired", "Y");
            errorNode.put("error", "데이터 처리 중 오류 발생");
            
            return errorNode;
        }
    }

    /**
     * RecipeSteps 노드를 처리하여 필드 누락 시 기본값 설정
     */
    private JsonNode processRecipeStepsNode(JsonNode recipeStepsNode) {
        try {
            // 배열인 경우 각 요소 처리
            if (recipeStepsNode.isArray()) {
                ArrayNode resultArray = objectMapper.createArrayNode();
                
                for (JsonNode node : recipeStepsNode) {
                    ObjectNode processedNode = objectMapper.createObjectNode();
                    
                    // 필수 필드 확인 및 기본값 설정
                    processedNode.put("description", node.has("description") ? node.get("description").asText() : "");
                    processedNode.put("img", node.has("img") ? node.get("img").asText() : "");
                    
                    resultArray.add(processedNode);
                }
                
                return resultArray;
            } 
            // 단일 객체인 경우
            else if (recipeStepsNode.isObject()) {
                ObjectNode processedNode = objectMapper.createObjectNode();
                
                // 필수 필드 확인 및 기본값 설정
                processedNode.put("description", recipeStepsNode.has("description") ? recipeStepsNode.get("description").asText() : "");
                processedNode.put("img", recipeStepsNode.has("img") ? recipeStepsNode.get("img").asText() : "");
                
                return processedNode;
            }
            
            // 배열이나 객체가 아닌 경우 기본 구조 반환
            ObjectNode defaultNode = objectMapper.createObjectNode();
            defaultNode.put("description", "");
            defaultNode.put("img", "");
            
            return defaultNode;
        } catch (Exception e) {
            // 오류 발생 시 기본 구조 반환
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("description", "데이터 처리 중 오류 발생");
            errorNode.put("img", "");
            
            return errorNode;
        }
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
