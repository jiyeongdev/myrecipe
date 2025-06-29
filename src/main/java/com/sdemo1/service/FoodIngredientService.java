package com.sdemo1.service;

import com.sdemo1.entity.FoodIngredient;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.repository.FoodIngredientRepository;
import com.sdemo1.repository.FoodQueryDSLRepository;
import com.sdemo1.request.FoodIngredientRequest;
import com.sdemo1.response.FoodIngredientResponse;
import com.sdemo1.dto.PageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodIngredientService {

    private final FoodIngredientRepository foodIngredientRepository;
    private final FoodQueryDSLRepository foodQueryDSLRepository;
    private final CategoryService categoryService;
    /**
     * 음식재료 등록 (배열) - Bulk Insert 사용
     */
    public String createFoodIngredients(Integer memberId, List<FoodIngredientRequest> requests) {
        int totalRequests = requests.size();
        int insertedCount = 0;

        // Bulk insert using INSERT IGNORE
        for (FoodIngredientRequest request : requests) {
            int result = foodIngredientRepository.insertIgnoreIngredient(
                memberId, 
                request.getFoodId(), 
                request.getFoodName()
            );
            insertedCount += result;
        }

        if (insertedCount == 0) {
            return "모든 음식재료가 이미 등록되어 있습니다.";
        } else if (insertedCount < totalRequests) {
            int duplicateCount = totalRequests - insertedCount;
            return String.format("총 %d개 중 %d개가 등록되었습니다. (%d개는 이미 등록된 항목)", 
                               totalRequests, insertedCount, duplicateCount);
        } else {
            return String.format("총 %d개의 음식재료가 모두 등록되었습니다.", insertedCount);
        }
    }

    /**
     * 음식재료 벌크 삭제
     * @param memberId 사용자 ID
     * @param foodIds 삭제할 음식재료 ID 목록
     * @return 삭제 결과 메시지
     */
    public String deleteFoodIngredients(Integer memberId, List<Integer> foodIds) {
        if (foodIds == null || foodIds.isEmpty()) {
            return "삭제할 음식재료가 선택되지 않았습니다.";
        }

        int deletedCount = foodIngredientRepository.deleteByMemberIdAndFoodIdIn(memberId, foodIds);
        
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
        // 1. 사용자가 등록한 음식재료들의 foodId 조회
        List<FoodIngredient> userIngredients = foodIngredientRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        
        if (userIngredients.isEmpty()) {
            // 등록한 음식재료가 없는 경우 빈 리스트 반환
            return List.of();
        }
        
        // 2. foodId 리스트 추출
        List<String> foodIdList = userIngredients.stream()
                .map(ingredient -> ingredient.getFoodId().toString())
                .collect(Collectors.toList());
        
        // 3. foodId들로 FoodItem 상세 정보 조회 (페이지네이션 없음)
        Page<FoodItem> foodItems = foodQueryDSLRepository.findIngredientByFoodId(foodIdList, 
                org.springframework.data.domain.Pageable.unpaged());
        
        // 4. CategoryService의 공통 메소드 사용하여 List로 변환
        return foodItems.getContent().stream()
                .map(categoryService::enrichWithCategoryInfo)
                .collect(Collectors.toList());
    }

    /**
     * FoodIngredient 엔티티를 FoodIngredientResponse로 변환
     */
    private FoodIngredientResponse convertToResponse(FoodIngredient foodIngredient) {
        return FoodIngredientResponse.builder()
                .fInId(foodIngredient.getFInId())
                .memberId(foodIngredient.getMemberId())
                .foodId(foodIngredient.getFoodId())
                .foodName(foodIngredient.getFoodName())
                .createdAt(foodIngredient.getCreatedAt())
                .modifiedAt(foodIngredient.getModifiedAt())
                .build();
    }


} 