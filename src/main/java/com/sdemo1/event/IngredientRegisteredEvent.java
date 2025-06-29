package com.sdemo1.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 재료 등록 완료 이벤트
 * - 사용자가 냉장고에 재료를 등록했을 때 발생
 * - 비동기 레시피 추천 작업을 트리거
 */
@Getter
public class IngredientRegisteredEvent extends ApplicationEvent {
    
    private final Integer memberId;
    private final List<Integer> registeredFoodIds;
    private final List<String> registeredFoodNames;
    private final int registeredCount;

    public IngredientRegisteredEvent(Object source, Integer memberId, 
                                   List<Integer> registeredFoodIds, 
                                   List<String> registeredFoodNames,
                                   int registeredCount) {
        super(source);
        this.memberId = memberId;
        this.registeredFoodIds = registeredFoodIds;
        this.registeredFoodNames = registeredFoodNames;
        this.registeredCount = registeredCount;
    }
} 