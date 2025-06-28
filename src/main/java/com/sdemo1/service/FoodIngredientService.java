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
     * 사용자가 등록한 음식재료들의 상세 정보 조회 (페이징)
     * @param memberId 사용자 ID
     * @param pageRequest 페이징 정보
     * @return 사용자가 등록한 음식재료들의 FoodItem 정보
     */
    @Transactional(readOnly = true)
    public Page<FoodItem> findIngredientsByMemberId(Integer memberId, PageRequestDto pageRequest) {
        // 1. 사용자가 등록한 음식재료들의 foodId 조회
        List<FoodIngredient> userIngredients = foodIngredientRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        
        if (userIngredients.isEmpty()) {
            // 등록한 음식재료가 없는 경우 빈 페이지 반환
            return new PageImpl<>(List.of(), pageRequest.toPageable(), 0);
        }
        
        // 2. foodId 리스트 추출
        List<String> foodIdList = userIngredients.stream()
                .map(ingredient -> ingredient.getFoodId().toString())
                .collect(Collectors.toList());
        
        // 3. foodId들로 FoodItem 상세 정보 조회
        Page<FoodItem> foodItems = foodQueryDSLRepository.findIngredientByFoodId(foodIdList, pageRequest.toPageable());
        
        // 4. CategoryService의 공통 메소드 사용
        return foodItems.map(categoryService::enrichWithCategoryInfo);
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