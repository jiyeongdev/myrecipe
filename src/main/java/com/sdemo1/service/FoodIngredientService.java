package com.sdemo1.service;

import com.sdemo1.entity.FoodIngredient;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.repository.FoodIngredientRepository;
import com.sdemo1.repository.FoodQueryDSLRepository;
import com.sdemo1.request.FoodIngredientRequest;
import com.sdemo1.event.IngredientRegisteredEvent;
import com.sdemo1.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FoodIngredientService {

    private final FoodIngredientRepository foodIngredientRepository;
    private final FoodQueryDSLRepository foodQueryDSLRepository;
    private final CategoryService categoryService;
    private final ApplicationEventPublisher eventPublisher;
    /**
     * 음식재료 등록 (배열) - Bulk Insert 사용
     * 등록 성공 시 비동기 레시피 추천 이벤트 발행
     */
    public String createFoodIngredients(Integer memberId, List<FoodIngredientRequest> requests) {
        // 매개변수 검증 및 로깅
        validateCreateFoodIngredientsParameters(memberId, requests);
        
        int totalRequests = requests.size();
        int insertedCount = 0;
        
        // 성공적으로 등록된 재료 정보 수집
        List<Integer> registeredFoodIds = new java.util.ArrayList<>();
        List<String> registeredFoodNames = new java.util.ArrayList<>();

        log.info("📋 재료 등록 작업 시작 - 사용자: {}, 요청된 재료 수: {}", memberId, totalRequests);

        // Bulk insert using INSERT IGNORE
        for (int i = 0; i < requests.size(); i++) {
            FoodIngredientRequest request = requests.get(i);
            
            try {
                log.debug("🔄 재료 등록 시도 [{}/{}] - ID: {}, 이름: '{}'", 
                         i + 1, totalRequests, request.getFoodID(), request.getFoodName());
                
                int result = foodIngredientRepository.insertIgnoreIngredient(
                    memberId, 
                    request.getFoodID(), 
                    request.getFoodName()
                );
                insertedCount += result;
                
                // 성공적으로 등록된 재료 정보 수집
                if (result > 0) {
                    registeredFoodIds.add(request.getFoodID());
                    registeredFoodNames.add(request.getFoodName());
                    log.debug("✅ 재료 등록 성공 [{}/{}] - ID: {}, 이름: '{}'", 
                             i + 1, totalRequests, request.getFoodID(), request.getFoodName());
                } else {
                    log.debug("⚠️ 재료 이미 등록됨 [{}/{}] - ID: {}, 이름: '{}'", 
                             i + 1, totalRequests, request.getFoodID(), request.getFoodName());
                }
                
            } catch (Exception e) {
                log.error("❌ 재료 등록 실패 [{}/{}] - ID: {}, 이름: '{}', 오류: {}", 
                         i + 1, totalRequests, request.getFoodID(), request.getFoodName(), e.getMessage());
                // 개별 재료 등록 실패는 전체 작업을 중단하지 않고 계속 진행
                // 필요에 따라 예외를 다시 던질 수도 있음
                throw e; // 트랜잭션 롤백을 위해 예외를 다시 던짐
            }
        }

        log.info("📊 재료 등록 결과 - 사용자: {}, 전체: {}개, 성공: {}개, 중복: {}개", 
                 memberId, totalRequests, insertedCount, (totalRequests - insertedCount));

        // 결과 메시지 생성
        String resultMessage;
        if (insertedCount == 0) {
            resultMessage = "모든 음식재료가 이미 등록되어 있습니다.";
        } else if (insertedCount < totalRequests) {
            int duplicateCount = totalRequests - insertedCount;
            resultMessage = String.format("총 %d개 중 %d개가 등록되었습니다. (%d개는 이미 등록된 항목)", 
                               totalRequests, insertedCount, duplicateCount);
        } else {
            resultMessage = String.format("총 %d개의 음식재료가 모두 등록되었습니다.", insertedCount);
        }

        // 새로 등록된 재료가 있는 경우 비동기 레시피 추천 이벤트 발행
        if (insertedCount > 0) {
            try {
                log.info("🚀 비동기 레시피 추천 이벤트 준비 - 사용자: {}, 등록된 재료 수: {}", 
                        memberId, insertedCount);
                        
                IngredientRegisteredEvent event = new IngredientRegisteredEvent(
                    this, 
                    memberId, 
                    registeredFoodIds, 
                    registeredFoodNames,
                    insertedCount
                );
                
                eventPublisher.publishEvent(event);
                
                log.info("📢 재료 등록 이벤트 발행 성공 - 사용자: {}, 등록된 재료: {}", 
                        memberId, registeredFoodNames);
                        
                if (log.isDebugEnabled()) {
                    String foodIdsList = registeredFoodIds.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                    log.debug("📝 이벤트 상세 정보 - 재료 ID 목록: [{}]", foodIdsList);
                }
                
            } catch (Exception e) {
                log.error("❌ 재료 등록 이벤트 발행 실패 - 사용자: {}, 오류: {}", 
                         memberId, e.getMessage(), e);
                // 이벤트 발행 실패해도 메인 기능(재료 등록)은 성공이므로 예외를 다시 던지지 않음
                // 필요시 모니터링 알림 등을 추가할 수 있음
            }
        } else {
            log.info("ℹ️ 새로 등록된 재료가 없어 레시피 추천 이벤트를 발행하지 않습니다 - 사용자: {}", memberId);
        }

        return resultMessage;
    }

    /**
     * 매개변수 검증 메소드
     * @param memberId 사용자 ID
     * @param requests 재료 등록 요청 목록
     * @throws CustomException 매개변수가 유효하지 않은 경우
     */
    private void validateCreateFoodIngredientsParameters(Integer memberId, List<FoodIngredientRequest> requests) {
        log.debug("🔍 매개변수 검증 시작 - memberId: {}, requests 크기: {}", 
                 memberId, requests != null ? requests.size() : "null");

        // 1. memberId 검증
        if (memberId == null) {
            log.error("❌ 매개변수 검증 실패: memberId가 null입니다");
            throw new CustomException("사용자 ID가 제공되지 않았습니다", 400);
        }
        
        if (memberId <= 0) {
            log.error("❌ 매개변수 검증 실패: memberId가 유효하지 않습니다. 값: {}", memberId);
            throw new CustomException("유효하지 않은 사용자 ID입니다: " + memberId, 400);
        }

        // 2. requests 리스트 검증
        if (requests == null) {
            log.error("❌ 매개변수 검증 실패: requests가 null입니다");
            throw new CustomException("재료 등록 요청 데이터가 제공되지 않았습니다", 400);
        }

        if (requests.isEmpty()) {
            log.error("❌ 매개변수 검증 실패: requests가 비어있습니다");
            throw new CustomException("등록할 재료가 없습니다", 400);
        }

        if (requests.size() > 100) { // 한 번에 너무 많은 재료 등록 방지
            log.error("❌ 매개변수 검증 실패: 요청된 재료 수가 너무 많습니다. 요청 수: {}", requests.size());
            throw new CustomException("한 번에 등록할 수 있는 재료는 최대 100개입니다", 400);
        }

        // 3. 각 FoodIngredientRequest 검증
        for (int i = 0; i < requests.size(); i++) {
            FoodIngredientRequest request = requests.get(i);
            
            if (request == null) {
                log.error("❌ 매개변수 검증 실패: {}번째 요청이 null입니다", i + 1);
                throw new CustomException(String.format("%d번째 재료 정보가 누락되었습니다", i + 1), 400);
            }

            // foodID 검증
            if (request.getFoodID() == null) {
                log.error("❌ 매개변수 검증 실패: {}번째 요청의 foodID가 null입니다. foodName: {}", 
                         i + 1, request.getFoodName());
                throw new CustomException(String.format("%d번째 재료의 ID가 누락되었습니다", i + 1), 400);
            }

            if (request.getFoodID() <= 0) {
                log.error("❌ 매개변수 검증 실패: {}번째 요청의 foodID가 유효하지 않습니다. foodID: {}, foodName: {}", 
                         i + 1, request.getFoodID(), request.getFoodName());
                throw new CustomException(String.format("%d번째 재료의 ID가 유효하지 않습니다: %d", i + 1, request.getFoodID()), 400);
            }

            // foodName 검증
            if (request.getFoodName() == null || request.getFoodName().trim().isEmpty()) {
                log.error("❌ 매개변수 검증 실패: {}번째 요청의 foodName이 비어있습니다. foodID: {}", 
                         i + 1, request.getFoodID());
                throw new CustomException(String.format("%d번째 재료의 이름이 누락되었습니다", i + 1), 400);
            }

            if (request.getFoodName().trim().length() > 100) { // 재료명 길이 제한
                log.error("❌ 매개변수 검증 실패: {}번째 요청의 foodName이 너무 깁니다. foodID: {}, foodName 길이: {}", 
                         i + 1, request.getFoodID(), request.getFoodName().length());
                throw new CustomException(String.format("%d번째 재료 이름이 너무 깁니다 (최대 100자)", i + 1), 400);
            }

            // 재료명 정규화 (앞뒤 공백 제거)
            request.setFoodName(request.getFoodName().trim());
        }

        // 4. 중복 foodID 검증
        List<Integer> foodIds = requests.stream()
                .map(FoodIngredientRequest::getFoodID)
                .collect(Collectors.toList());
        
        long uniqueFoodIdCount = foodIds.stream().distinct().count();
        if (uniqueFoodIdCount != foodIds.size()) {
            log.error("❌ 매개변수 검증 실패: 중복된 foodID가 있습니다. 전체: {}, 고유: {}", 
                     foodIds.size(), uniqueFoodIdCount);
            throw new CustomException("요청 목록에 중복된 재료가 있습니다", 400);
        }

        log.info("✅ 매개변수 검증 완료 - 사용자: {}, 유효한 재료 요청 수: {}", memberId, requests.size());
        
        // 검증된 재료 목록 로깅 (디버그 레벨)
        if (log.isDebugEnabled()) {
            String ingredientSummary = requests.stream()
                    .map(r -> String.format("ID:%d(%s)", r.getFoodID(), r.getFoodName()))
                    .collect(Collectors.joining(", "));
            log.debug("📝 등록 예정 재료 목록: {}", ingredientSummary);
        }
    }

    /**
     * 삭제 매개변수 검증 메소드
     * @param memberId 사용자 ID
     * @param foodIds 삭제할 재료 ID 목록
     * @throws CustomException 매개변수가 유효하지 않은 경우
     */
    private void validateDeleteFoodIngredientsParameters(Integer memberId, List<Integer> foodIds) {
        log.debug("🔍 삭제 매개변수 검증 시작 - memberId: {}, foodIds 크기: {}", 
                 memberId, foodIds != null ? foodIds.size() : "null");

        // 1. memberId 검증
        if (memberId == null) {
            log.error("❌ 삭제 매개변수 검증 실패: memberId가 null입니다");
            throw new CustomException("사용자 ID가 제공되지 않았습니다", 400);
        }
        
        if (memberId <= 0) {
            log.error("❌ 삭제 매개변수 검증 실패: memberId가 유효하지 않습니다. 값: {}", memberId);
            throw new CustomException("유효하지 않은 사용자 ID입니다: " + memberId, 400);
        }

        // 2. foodIds 리스트 검증
        if (foodIds == null) {
            log.error("❌ 삭제 매개변수 검증 실패: foodIds가 null입니다");
            throw new CustomException("삭제할 재료 ID 목록이 제공되지 않았습니다", 400);
        }

        if (foodIds.isEmpty()) {
            log.error("❌ 삭제 매개변수 검증 실패: foodIds가 비어있습니다");
            throw new CustomException("삭제할 재료가 선택되지 않았습니다", 400);
        }

        if (foodIds.size() > 50) { // 한 번에 너무 많은 재료 삭제 방지
            log.error("❌ 삭제 매개변수 검증 실패: 요청된 삭제 재료 수가 너무 많습니다. 요청 수: {}", foodIds.size());
            throw new CustomException("한 번에 삭제할 수 있는 재료는 최대 50개입니다", 400);
        }

        // 3. 각 foodId 검증
        for (int i = 0; i < foodIds.size(); i++) {
            Integer foodId = foodIds.get(i);
            
            if (foodId == null) {
                log.error("❌ 삭제 매개변수 검증 실패: {}번째 foodId가 null입니다", i + 1);
                throw new CustomException(String.format("%d번째 재료 ID가 누락되었습니다", i + 1), 400);
            }

            if (foodId <= 0) {
                log.error("❌ 삭제 매개변수 검증 실패: {}번째 foodId가 유효하지 않습니다. foodId: {}", 
                         i + 1, foodId);
                throw new CustomException(String.format("%d번째 재료 ID가 유효하지 않습니다: %d", i + 1, foodId), 400);
            }
        }

        // 4. 중복 foodId 검증
        long uniqueFoodIdCount = foodIds.stream().distinct().count();
        if (uniqueFoodIdCount != foodIds.size()) {
            log.error("❌ 삭제 매개변수 검증 실패: 중복된 foodId가 있습니다. 전체: {}, 고유: {}", 
                     foodIds.size(), uniqueFoodIdCount);
            throw new CustomException("삭제 목록에 중복된 재료 ID가 있습니다", 400);
        }

        log.info("✅ 삭제 매개변수 검증 완료 - 사용자: {}, 유효한 삭제 요청 수: {}", memberId, foodIds.size());
    }

    /**
     * 음식재료 벌크 삭제
     * @param memberId 사용자 ID
     * @param foodIds 삭제할 음식재료 ID 목록
     * @return 삭제 결과 메시지
     */
    public String deleteFoodIngredients(Integer memberId, List<Integer> foodIds) {
        // 매개변수 검증
        validateDeleteFoodIngredientsParameters(memberId, foodIds);
        
        if (foodIds == null || foodIds.isEmpty()) {
            return "삭제할 음식재료가 선택되지 않았습니다.";
        }

        log.info("🗑️ 재료 삭제 작업 시작 - 사용자: {}, 삭제 요청 재료 수: {}", memberId, foodIds.size());
        
        if (log.isDebugEnabled()) {
            String foodIdsList = foodIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            log.debug("📝 삭제 예정 재료 ID 목록: [{}]", foodIdsList);
        }

        int deletedCount = foodIngredientRepository.deleteByMemberIdAndFoodIdIn(memberId, foodIds);
        
        log.info("📊 재료 삭제 결과 - 사용자: {}, 요청: {}개, 실제 삭제: {}개", 
                 memberId, foodIds.size(), deletedCount);
        
        if (deletedCount == 0) {
            return "선택한 음식재료가 존재하지 않거나 이미 삭제되었습니다.";
        } else if (deletedCount < foodIds.size()) {
            int notFoundCount = foodIds.size() - deletedCount;
            return String.format("총 %d개 중 %d개가 삭제되었습니다. (%d개는 존재하지 않는 항목)", 
                               foodIds.size(), deletedCount, notFoundCount);
        } else {
            return String.format("총 %d개의 음식재료가 모두 삭제되었습니다.", deletedCount);
        }
    }


    /**
     * 사용자가 등록한 음식재료들의 상세 정보 전체 조회 (페이지네이션 없음)
     * @param memberId 사용자 ID
     * @return 사용자가 등록한 음식재료들의 FoodItem 정보 전체 목록
     */
    @Transactional(readOnly = true)
    public List<FoodItem> findAllIngredientsByMemberId(Integer memberId) {
        // 매개변수 검증
        if (memberId == null) {
            log.error("❌ 조회 매개변수 검증 실패: memberId가 null입니다");
            throw new CustomException("사용자 ID가 제공되지 않았습니다", 400);
        }
        
        if (memberId <= 0) {
            log.error("❌ 조회 매개변수 검증 실패: memberId가 유효하지 않습니다. 값: {}", memberId);
            throw new CustomException("유효하지 않은 사용자 ID입니다: " + memberId, 400);
        }

        log.info("🔍 재료 목록 조회 시작 - 사용자: {}", memberId);

        // 1. 사용자가 등록한 음식재료들의 foodId 조회
        List<FoodIngredient> userIngredients = foodIngredientRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        
        if (userIngredients.isEmpty()) {
            log.info("ℹ️ 재료 목록 조회 결과 - 사용자: {}, 등록된 재료 없음", memberId);
            // 등록한 음식재료가 없는 경우 빈 리스트 반환
            return List.of();
        }
        
        log.debug("📋 사용자 등록 재료 수: {} - 사용자: {}", userIngredients.size(), memberId);
        
        // 2. foodId 리스트 추출
        List<String> foodIdList = userIngredients.stream()
                .map(ingredient -> ingredient.getFoodId().toString())
                .collect(Collectors.toList());
        
        if (log.isDebugEnabled()) {
            String foodIdsList = String.join(", ", foodIdList);
            log.debug("📝 조회할 재료 ID 목록: [{}] - 사용자: {}", foodIdsList, memberId);
        }
        
        // 3. foodId들로 FoodItem 상세 정보 조회 (페이지네이션 없음)
        Page<FoodItem> foodItems = foodQueryDSLRepository.findIngredientByFoodId(foodIdList, 
                org.springframework.data.domain.Pageable.unpaged());
        
        // 4. CategoryService의 공통 메소드 사용하여 List로 변환
        List<FoodItem> result = foodItems.getContent().stream()
                .map(categoryService::enrichWithCategoryInfo)
                .collect(Collectors.toList());
                
        log.info("✅ 재료 목록 조회 완료 - 사용자: {}, 조회된 재료 수: {}", memberId, result.size());
        
        return result;
    }


} 