package com.sdemo1.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.EnrichedRecommendation;
import com.sdemo1.request.CookRecipeRequest;
import com.sdemo1.response.CookRecipeResponse;
import com.sdemo1.security.CustomUserDetails;
import com.sdemo1.service.CookRecipeService;
import com.sdemo1.service.RecipeRecommendationService;
import com.sdemo1.util.JwtTokenUtil;
import com.sdemo1.dto.PageRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/cook/recipes")
@RequiredArgsConstructor
public class CookRecipeController {

    private final CookRecipeService cookRecipeService;
    private final RecipeRecommendationService recommendationService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping
    public ResponseEntity<Integer> createRecipe(@RequestBody CookRecipeRequest request) {
        try {
            Integer cookId = cookRecipeService.createRecipe(request);
            return ResponseEntity.ok(cookId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/detail/{cookId}")
    public ApiResponse<List<CookRecipeResponse>> getRecipesByCookID(@PathVariable("cookId") Integer cookId) {
        try {
            List<CookRecipeResponse> recipes = cookRecipeService.getRecipesByCookId(cookId);
            return new ApiResponse<>(
                null,
                recipes,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/{cookId}")
    public ApiResponse<Void> updateRecipe(
            @PathVariable("cookId")  Integer cookId,
            @RequestBody CookRecipeRequest request) {
        try {
            cookRecipeService.updateRecipe(cookId, request);
            return new ApiResponse<>(
                "레시피가 성공적으로 업데이트되었습니다.",
                null,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                "레시피 업데이트 실패하였습니다: " + e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * 레시피 삭제 API
     * @param cookId 삭제할 레시피 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{cookId}")
    public ApiResponse<Void> deleteRecipe(@PathVariable("cookId") Integer cookId) {
        try {
            cookRecipeService.deleteRecipe(cookId);
            return new ApiResponse<>(
                "레시피가 성공적으로 삭제되었습니다.",
                null,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                "레시피 삭제 실패하였습니다: " + e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * 맞춤 추천 조회 API
     * 
     * 사용자의 보유 재료를 기반으로 한 맞춤 레시피 추천
     * 
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 맞춤 추천 레시피 목록
     */
    @GetMapping("/recommendations")
    public ApiResponse<List<EnrichedRecommendation>> getPersonalizedRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            Integer memberId = jwtTokenUtil.extractMemberIdFromAuth();
            
            // 맞춤 추천 조회 (Redis에서)
            List<EnrichedRecommendation> personalizedRecommendations = 
                    recommendationService.getCachedRecommendations(memberId);
            
            log.info("맞춤 추천 조회 완료 - 사용자: {}, 추천: {}개", 
                    memberId, personalizedRecommendations.size());
            
            return new ApiResponse<>(
                    null,
                    personalizedRecommendations,
                    HttpStatus.OK
            );
            
        } catch (Exception e) {
            log.error("맞춤 추천 조회 실패", e);
            return new ApiResponse<>(
                    "맞춤 추천을 불러오는 중 오류가 발생했습니다: " + e.getMessage(),
                    new ArrayList<>(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 전체 피드 조회 API
     * 
     * 내 레시피를 제외한 모든 레시피 조회 (페이징 지원)
     * 
     * @param userDetails 현재 로그인한 사용자 정보
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 10)
     * @return 전체 레시피 목록
     */
    @GetMapping("/feed")
    public ApiResponse<List<CookRecipeResponse>> getAllRecipesFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        
        try {
            Integer memberId = jwtTokenUtil.extractMemberIdFromAuth();
            
            // 페이징 정보 설정
            PageRequestDto pageRequest = new PageRequestDto(page, size);
            
            // 전체 레시피 조회 (내 레시피 제외, 페이징 적용)
            List<CookRecipeResponse> allRecipes = 
                    cookRecipeService.getAllRecipesExceptMine(memberId, pageRequest.getSize());
            
            return new ApiResponse<>(
                    null,
                    allRecipes,
                    HttpStatus.OK
            );
            
        } catch (Exception e) {
            log.error("전체 피드 조회 실패", e);
            return new ApiResponse<>(
                    "전체 피드를 불러오는 중 오류가 발생했습니다: " + e.getMessage(),
                    new ArrayList<>(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<CookRecipeResponse>> getRecipesByUserId(@PathVariable("userId") Integer userId) {
        try {
            List<CookRecipeResponse> recipes = cookRecipeService.getRecipesByUserId(userId);
            return new ApiResponse<>(
                null,
                recipes,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }
}