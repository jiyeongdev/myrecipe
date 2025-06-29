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

    /**
     * 음식재료 등록 API (배열) - Bulk Insert
     */
    @PostMapping("/my-ingredient")
    public ResponseEntity<ApiResponse<String>> createFoodIngredients(
            @RequestBody List<FoodIngredientRequest> requests) {
        try {
            // JWT 토큰에서 member_id 추출 (안전한 방식)
            Integer memberId = extractMemberIdFromAuth();
            
            log.debug("🔐 인증된 사용자 ID: {}", memberId);

            String result = foodIngredientService.createFoodIngredients(memberId, requests);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>(result, null, HttpStatus.OK));

        } catch (CustomException e) {
            // 비즈니스 로직 예외 (400 Bad Request)
            log.warn("⚠️ 재료 등록 비즈니스 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
                    
        } catch (NumberFormatException e) {
            // JWT 토큰의 사용자 ID 파싱 오류
            log.error("❌ 사용자 ID 파싱 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증 정보가 올바르지 않습니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (SecurityException e) {
            // 인증/인가 관련 오류
            log.error("❌ 보안 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증이 필요합니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (Exception e) {
            // 기타 예상하지 못한 오류
            log.error("💥 재료 등록 중 예상치 못한 오류 발생", e);
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
            Integer memberId = extractMemberIdFromAuth();
            
            log.debug("🔐 인증된 사용자 ID: {}", memberId);

            // 전체 조회
            List<FoodItem> ingredients = foodIngredientService.findAllIngredientsByMemberId(memberId);

            return new ApiResponse<>(null, ingredients, HttpStatus.OK);

        } catch (CustomException e) {
            // 비즈니스 로직 예외
            log.warn("⚠️ 재료 목록 조회 비즈니스 오류: {}", e.getMessage());
            return new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST);
            
        } catch (SecurityException | NumberFormatException e) {
            // 인증 관련 예외
            log.error("❌ 재료 목록 조회 인증 오류: {}", e.getMessage());
            return new ApiResponse<>("인증 정보가 올바르지 않습니다", null, HttpStatus.UNAUTHORIZED);
            
        } catch (Exception e) {
            log.error("💥 재료 목록 조회 중 예상치 못한 오류 발생", e);
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
        try {
            // JWT 토큰에서 member_id 추출 (안전한 방식)
            Integer memberId = extractMemberIdFromAuth();
            
            log.debug("🔐 인증된 사용자 ID: {}", memberId);

            String result = foodIngredientService.deleteFoodIngredients(memberId, foodIds);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(result, null, HttpStatus.OK));

        } catch (CustomException e) {
            // 비즈니스 로직 예외 (400 Bad Request)
            log.warn("⚠️ 재료 삭제 비즈니스 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
                    
        } catch (NumberFormatException e) {
            // JWT 토큰의 사용자 ID 파싱 오류
            log.error("❌ 사용자 ID 파싱 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증 정보가 올바르지 않습니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (SecurityException e) {
            // 인증/인가 관련 오류
            log.error("❌ 보안 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("인증이 필요합니다", null, HttpStatus.UNAUTHORIZED));
                    
        } catch (Exception e) {
            // 기타 예상하지 못한 오류
            log.error("💥 재료 삭제 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("음식재료 삭제 중 오류가 발생했습니다: " + e.getMessage(), 
                                          null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * JWT 토큰에서 사용자 ID를 안전하게 추출하는 메소드
     * @return 사용자 ID
     * @throws SecurityException 인증 정보가 없거나 잘못된 경우
     * @throws NumberFormatException 사용자 ID가 숫자가 아닌 경우
     */
    private Integer extractMemberIdFromAuth() {
        try {
            // 1. SecurityContext에서 Authentication 객체 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // 2. Authentication 객체 null 체크
            if (auth == null) {
                log.error("❌ Authentication 객체가 null입니다");
                throw new SecurityException("인증 정보가 없습니다");
            }
            
            // 3. 인증 여부 확인
            if (!auth.isAuthenticated()) {
                log.error("❌ 인증되지 않은 사용자입니다");
                throw new SecurityException("인증되지 않은 사용자입니다");
            }
            
            // 4. principal 확인
            Object principal = auth.getPrincipal();
            if (principal == null) {
                log.error("❌ Principal이 null입니다");
                throw new SecurityException("인증 정보가 올바르지 않습니다");
            }
            
            // 5. 사용자 이름(ID) 추출
            String memberIdStr = auth.getName();
            if (memberIdStr == null || memberIdStr.trim().isEmpty()) {
                log.error("❌ 사용자 ID가 비어있습니다. Principal: {}", principal);
                throw new SecurityException("사용자 ID를 찾을 수 없습니다");
            }
            
            // 6. 숫자로 변환
            try {
                Integer memberId = Integer.parseInt(memberIdStr.trim());
                
                if (memberId <= 0) {
                    log.error("❌ 사용자 ID가 유효하지 않습니다: {}", memberId);
                    throw new SecurityException("유효하지 않은 사용자 ID입니다");
                }
                
                log.debug("✅ 사용자 ID 추출 성공: {}", memberId);
                return memberId;
                
            } catch (NumberFormatException e) {
                log.error("❌ 사용자 ID 형식이 잘못되었습니다: '{}', 오류: {}", memberIdStr, e.getMessage());
                throw new NumberFormatException("사용자 ID가 숫자 형식이 아닙니다: " + memberIdStr);
            }
            
        } catch (SecurityException | NumberFormatException e) {
            // 이미 처리된 예외는 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("❌ 사용자 ID 추출 중 예상치 못한 오류 발생", e);
            throw new SecurityException("인증 정보 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}
