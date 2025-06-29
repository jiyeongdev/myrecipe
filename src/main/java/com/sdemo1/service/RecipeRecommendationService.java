package com.sdemo1.service;

import com.sdemo1.event.IngredientRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 비동기 레시피 추천 서비스
 * - 재료 등록 후 백그라운드에서 레시피 추천 작업 수행
 * - 스레드 풀을 활용하여 메인 요청 흐름과 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeRecommendationService {

    /**
     * 재료 등록 트랜잭션이 성공적으로 커밋된 후에 실행
     * - AFTER_COMMIT: 트랜잭션 커밋 후에만 이벤트 처리
     * - 메인 트랜잭션 실패 시 레시피 추천 작업이 실행되지 않음
     */
    @Async("recipeRecommendationExecutor")  // 전용 스레드 풀 사용
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIngredientRegistered(IngredientRegisteredEvent event) {
        log.info("🚀 [스레드: {}] 재료 등록 이벤트 수신 - 사용자: {}, 등록된 재료 수: {}", 
                Thread.currentThread().getName(), 
                event.getMemberId(), 
                event.getRegisteredCount());

        try {
            // 비동기 레시피 추천 작업 시작
            CompletableFuture<Void> recommendationTask = processRecipeRecommendation(
                event.getMemberId(), 
                event.getRegisteredFoodIds(),
                event.getRegisteredFoodNames()
            );

            // 완료 후 콜백 (선택사항)
            recommendationTask.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("❌ 레시피 추천 작업 실패 - 사용자: {}, 오류: {}", 
                            event.getMemberId(), throwable.getMessage());
                } else {
                    log.info("✅ 레시피 추천 작업 완료 - 사용자: {}", event.getMemberId());
                }
            });

        } catch (Exception e) {
            log.error("💥 레시피 추천 이벤트 처리 중 예외 발생 - 사용자: {}, 오류: {}", 
                    event.getMemberId(), e.getMessage(), e);
        }
    }

    /**
     * 실제 레시피 추천 로직 (CompletableFuture로 래핑)
     */
    @Async("recipeRecommendationExecutor")
    public CompletableFuture<Void> processRecipeRecommendation(Integer memberId, 
                                                              List<Integer> foodIds, 
                                                              List<String> foodNames) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("🔍 [스레드: {}] 레시피 추천 작업 시작 - 사용자: {}", 
                        Thread.currentThread().getName(), memberId);
                
                // 1단계: 사용자의 모든 보유 재료 조회
                // (실제로는 DB에서 조회하겠지만, 예시로 파라미터 사용)
                
                // 2단계: 유사한 재료를 포함한 레시피 검색
                List<RecommendedRecipe> recommendations = searchSimilarRecipes(foodNames);
                
                // 3단계: 추천 점수 계산 (보유 재료와의 매칭률)
                List<RecommendedRecipe> scoredRecipes = calculateRecommendationScores(recommendations, foodNames);
                
                // 4단계: 추천 결과 저장 (DB/Redis)
                saveRecommendationResults(memberId, scoredRecipes);
                
                // 5단계: 사용자에게 알림 (선택사항)
                sendRecommendationNotification(memberId, scoredRecipes.size());
                
                log.info("✨ 레시피 추천 완료 - 사용자: {}, 추천 개수: {}", 
                        memberId, scoredRecipes.size());
                
            } catch (Exception e) {
                log.error("🚨 레시피 추천 작업 중 오류 - 사용자: {}, 오류: {}", 
                        memberId, e.getMessage(), e);
                throw e;
            }
        }, java.util.concurrent.ForkJoinPool.commonPool());
    }

    /**
     * 유사한 재료를 포함한 레시피 검색
     * (실제로는 외부 API 호출이나 복잡한 DB 쿼리)
     */
    private List<RecommendedRecipe> searchSimilarRecipes(List<String> ingredientNames) {
        // 시뮬레이션: 실제로는 시간이 오래 걸리는 작업
        try {
            Thread.sleep(2000); // 2초 대기 (외부 API 호출 시뮬레이션)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("레시피 검색 중 인터럽트 발생", e);
        }
        
        log.info("🔎 레시피 검색 완료 - 검색된 재료: {}", ingredientNames);
        
        // 더미 데이터 반환 (실제로는 DB나 외부 API에서 조회)
        return List.of(
            new RecommendedRecipe("김치찌개", ingredientNames, 0.0),
            new RecommendedRecipe("된장찌개", ingredientNames, 0.0),
            new RecommendedRecipe("계란볶음밥", ingredientNames, 0.0)
        );
    }

    /**
     * 추천 점수 계산 (재료 매칭률 기반)
     */
    private List<RecommendedRecipe> calculateRecommendationScores(List<RecommendedRecipe> recipes, 
                                                                List<String> userIngredients) {
        return recipes.stream()
                .peek(recipe -> {
                    // 간단한 매칭률 계산 (실제로는 더 복잡한 알고리즘 사용)
                    double score = Math.random() * 100; // 더미 점수
                    recipe.setMatchingScore(score);
                })
                .sorted((r1, r2) -> Double.compare(r2.getMatchingScore(), r1.getMatchingScore()))
                .toList();
    }

    /**
     * 추천 결과 저장 (실제로는 DB나 Redis에 저장)
     */
    private void saveRecommendationResults(Integer memberId, List<RecommendedRecipe> recommendations) {
        log.info("💾 추천 결과 저장 - 사용자: {}, 저장할 레시피 수: {}", 
                memberId, recommendations.size());
        
        // 실제로는 DB나 Redis에 저장하는 로직
        // recommendationRepository.saveAll(...)
    }

    /**
     * 추천 완료 알림 전송
     */
    @Async("notificationExecutor")  // 별도 스레드 풀 사용
    public void sendRecommendationNotification(Integer memberId, int recommendationCount) {
        try {
            Thread.sleep(500); // 알림 전송 시뮬레이션
            log.info("📱 추천 완료 알림 전송 - 사용자: {}, 추천 레시피 수: {}", 
                    memberId, recommendationCount);
            
            // 실제로는 WebSocket, 푸시 알림, 이메일 등으로 알림 전송
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("알림 전송 중 인터럽트 발생 - 사용자: {}", memberId);
        }
    }

    /**
     * 추천 레시피 DTO (내부 클래스)
     */
    private static class RecommendedRecipe {
        private String recipeName;
        private List<String> matchedIngredients;
        private double matchingScore;

        public RecommendedRecipe(String recipeName, List<String> matchedIngredients, double matchingScore) {
            this.recipeName = recipeName;
            this.matchedIngredients = matchedIngredients;
            this.matchingScore = matchingScore;
        }

        public void setMatchingScore(double matchingScore) {
            this.matchingScore = matchingScore;
        }

        public double getMatchingScore() {
            return matchingScore;
        }
    }
} 