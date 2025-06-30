package com.sdemo1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.sdemo1.service.FoodIngredientService;
import com.sdemo1.util.JwtTokenUtil;
import com.sdemo1.request.FoodIngredientRequest;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ck")
@RequiredArgsConstructor
public class FoodController {

    private final FoodIngredientService foodIngredientService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 음식재료 등록 API (배열) - Bulk Insert
     */
    @PostMapping("/my-ingredient")
    public ResponseEntity<ApiResponse<String>> createFoodIngredients(
            @RequestBody List<FoodIngredientRequest> requests) {
        try {
           // JWT 토큰에서 member_id 추출 (안전한 방식)
           Integer memberId = jwtTokenUtil.extractMemberIdFromAuth();
             
            String result = foodIngredientService.createFoodIngredients(memberId, requests);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>(result, null, HttpStatus.OK));

        } catch (CustomException e) {
            // 비즈니스 로직 예외 (400 Bad Request)
            log.warn("재료 등록 비즈니스 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
                    
        } catch (NumberFormatException e) {
            // JWT 토큰의 사용자 ID 파싱 오류
            log.error("사용자 ID 파싱 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증 정보가 올바르지 않습니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (SecurityException e) {
            // 인증/인가 관련 오류
            log.error("보안 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증이 필요합니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (Exception e) {
            // 기타 예상하지 못한 오류
            log.error("재료 등록 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("음식재료 등록 중 오류가 발생했습니다: " + e.getMessage(), 
                                          null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 사용자의 음식재료 목록 조회 API (전체 조회)
     */
    @GetMapping("/my-ingredient")
    public ApiResponse<List<FoodItem>> getFoodIngredients() {
        try {
              // JWT 토큰에서 member_id 추출 (안전한 방식)
              Integer memberId = jwtTokenUtil.extractMemberIdFromAuth();
            
            // 전체 조회
            List<FoodItem> ingredients = foodIngredientService.findAllIngredientsByMemberId(memberId);

            return new ApiResponse<>(null, ingredients, HttpStatus.OK);

        } catch (CustomException e) {
            // 비즈니스 로직 예외
            log.warn("재료 목록 조회 비즈니스 오류: {}", e.getMessage());
            return new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST);
            
        } catch (SecurityException | NumberFormatException e) {
            // 인증 관련 예외
            log.error("재료 목록 조회 인증 오류: {}", e.getMessage());
            return new ApiResponse<>("인증 정보가 올바르지 않습니다", null, HttpStatus.UNAUTHORIZED);
            
        } catch (Exception e) {
            log.error("재료 목록 조회 중 예상치 못한 오류 발생", e);
            return new ApiResponse<>("음식재료 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), 
                                   null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 음식재료 삭제 API
     */
    @DeleteMapping("/my-ingredient")
    public ResponseEntity<ApiResponse<String>> deleteFoodIngredients(
            @RequestBody List<Integer> foodIds) {
        try {  // JWT 토큰에서 member_id 추출 (안전한 방식)
            Integer memberId = jwtTokenUtil.extractMemberIdFromAuth();
           
            String result = foodIngredientService.deleteFoodIngredients(memberId, foodIds);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(result, null, HttpStatus.OK));

        } catch (CustomException e) {
            // 비즈니스 로직 예외 (400 Bad Request)
            log.warn("재료 삭제 비즈니스 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
                    
        } catch (NumberFormatException e) {
            // JWT 토큰의 사용자 ID 파싱 오류
            log.error("사용자 ID 파싱 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증 정보가 올바르지 않습니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (SecurityException e) {
            // 인증/인가 관련 오류
            log.error("보안 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증이 필요합니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (Exception e) {
            // 기타 예상하지 못한 오류
            log.error("재료 삭제 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("음식재료 삭제 중 오류가 발생했습니다: " + e.getMessage(), 
                                          null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


}
