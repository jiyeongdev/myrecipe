package com.sdemo1.response;

import com.sdemo1.dto.EnrichedRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * SNS 피드 응답 - 하이브리드 구조
 *
 * 맞춤 추천 (상단)
 * - 토마토 파스타 (내 재료: 토마토, 면)
 * - 마늘볶음밥 (내 재료: 마늘, 밥)
 *
 * 모든 레시피 (하단)
 * - 양파볶음 (추천받은 레시피 표시)
 * - 김치찌개
 * - 된장찌개
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SNSFeedResponse {
    
    /**
     * 당신을 위한 맞춤 추천 섹션
     */
    private RecommendationSection personalizedRecommendations;
    
    /**
     * 모든 레시피 섹션
     */
    private AllRecipesSection allRecipes;
    
    /**
     * 맞춤 추천 섹션
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationSection {
        private boolean hasRecommendations;        // 추천이 있는지 여부
        private String sectionTitle;               // "당신을 위한 맞춤 추천"
        private String emptyMessage;               // 추천이 없을 때 메시지
        private List<EnrichedRecommendation> recommendations;  // 추천 레시피들
        private int totalCount;                    // 전체 추천 개수
    }
    
    /**
     * 전체 레시피 섹션
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllRecipesSection {
        private String sectionTitle;               // "모든 레시피"
        private List<RecipeWithRecommendationFlag> recipes;  // 레시피 목록 (추천 표시 포함)
        private int totalCount;                    // 전체 레시피 개수
        private boolean hasMore;                   // 더 많은 레시피가 있는지
    }
    
    /**
     * 추천 플래그가 포함된 레시피
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeWithRecommendationFlag {
        private CookRecipeResponse recipe;         // 기본 레시피 정보
        private boolean isRecommended;             // 내게 추천된 레시피인지
        private String recommendationBadge;       // "맞춤 추천" 같은 배지
        private String matchingDescription;       // "내 재료 5/6개 보유" (추천된 경우만)
    }
    
    /**
     * 빈 피드 생성 (추천도 없고 레시피도 없는 경우)
     */
    public static SNSFeedResponse createEmpty() {
        return SNSFeedResponse.builder()
                .personalizedRecommendations(RecommendationSection.builder()
                        .hasRecommendations(false)
                        .sectionTitle("당신을 위한 맞춤 추천")
                        .emptyMessage("재료를 더 추가하면 맞춤 레시피를 추천해드려요!")
                        .recommendations(List.of())
                        .totalCount(0)
                        .build())
                .allRecipes(AllRecipesSection.builder()
                        .sectionTitle("모든 레시피")
                        .recipes(List.of())
                        .totalCount(0)
                        .hasMore(false)
                        .build())
                .build();
    }
} 