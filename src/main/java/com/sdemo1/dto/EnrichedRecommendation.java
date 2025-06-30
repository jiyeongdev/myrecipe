package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.sdemo1.request.CookRecipeRequest;
import com.sdemo1.response.CookRecipeResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * 추천 결과를 위한 통합 DTO (LightRecommendation, RecommendationResult 통합)
 * 
 * 장점:
 * - 단일 DTO로 모든 추천 관련 정보 관리 (유지보수 편의성)
 * - 완전한 레시피 정보 포함 (recipeInfo)
 * - 편의 메서드로 쉽게 접근 (getCookId(), getCookTitle() 등)
 * - Redis 캐싱과 API 응답에 모두 사용 가능
 * - 매칭률, 부족한 재료 등 추천 관련 정보 포함
 * 
 * 사용처:
 * - 내부 계산 과정에서 사용
 * - 최종 API 응답에서 사용 (SNS 화면용)
 * - Redis 캐싱용으로도 사용
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrichedRecommendation {
    
    // 매칭 분석 결과
    private Double matchingRate;
    @JsonIgnore
    private Integer matchedCount;
    @JsonIgnore
    private Integer totalIngredients;
    @JsonIgnore
    private List<String> missingIngredients;
    
    // 완전한 레시피 정보 (최종 응답용)
    private CookRecipeResponse recipeInfo;
    
    /**
     * 매칭 상태 설명 생성
     * @return "내 재료 5/6개 보유"
     */
    @JsonIgnore
    public String getMatchingDescription() {
        if (matchedCount == null || totalIngredients == null) {
            return null;
        }
        return String.format("내 재료 %d/%d개 보유", matchedCount, totalIngredients);
    }
    
    /**
     * 매칭률을 백분율 문자열로 반환
     * @return "85%"
     */
    public String getMatchingRatePercent() {
        if (matchingRate == null) {
            return null;
        }
        return String.format("%.0f%%", matchingRate);
    }
    
    /**
     * 작성자 표시용 텍스트
     * @return "홍길동님의 레시피"
     */
    public String getDisplay() {
        if (recipeInfo != null && recipeInfo.getUserId() != null) {
            return "사용자" + recipeInfo.getUserId() + "님의 레시피";
        }
        return null;
    }
    
    /**
     * 편의 메서드: cookId 조회
     */
    @JsonIgnore
    public Integer getCookId() {
        return recipeInfo != null ? recipeInfo.getCookId() : null;
    }
    
    /**
     * 편의 메서드: 레시피 제목 조회
     */
    @JsonIgnore
    public String getCookTitle() {
        return recipeInfo != null ? recipeInfo.getCookTitle() : null;
    }
    
    /**
     * 편의 메서드: 작성자 ID 조회
     */
    @JsonIgnore
    public Integer getAuthorId() {
        return recipeInfo != null ? recipeInfo.getUserId() : null;
    }
    
    /**
     * 편의 메서드: 레시피 이미지 조회
     */
    @JsonIgnore
    public String getCookImg() {
        return recipeInfo != null ? recipeInfo.getCookImg() : null;
    }
    
    /**
     * 편의 메서드: 재료 목록 조회
     */
    @JsonIgnore
    public List<CookRecipeRequest.Ingredient> getIngredients() {
        return recipeInfo != null ? recipeInfo.getIngredients() : null;
    }
    
    /**
     * 편의 메서드: 레시피 단계 조회
     */
    @JsonIgnore
    public List<CookRecipeRequest.RecipeStepDetail> getRecipeSteps() {
        return recipeInfo != null ? recipeInfo.getRecipeSteps() : null;
    }
    
    /**
     * CookRecipeResponse와 결합하여 최종 응답용 생성
     */
    public static EnrichedRecommendation fromCookRecipeResponse(
            CookRecipeResponse recipe, 
            double matchingRate, 
            int matchedCount, 
            int totalIngredients,
            List<String> missingIngredients) {
        
        return EnrichedRecommendation.builder()
                .matchingRate(matchingRate)
                .matchedCount(matchedCount)
                .totalIngredients(totalIngredients)
                .missingIngredients(missingIngredients)
                .recipeInfo(recipe)  // 완전한 레시피 정보 포함
                .build();
    }
    
    /**
     * 내부 계산 결과를 최종 응답용으로 변환
     */
    public EnrichedRecommendation withRecipeInfo(CookRecipeResponse recipe) {
        this.recipeInfo = recipe;
        return this;
    }
} 