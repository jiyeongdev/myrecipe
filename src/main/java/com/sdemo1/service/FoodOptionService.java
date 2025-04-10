package com.sdemo1.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sdemo1.entity.FoodOption;
import com.sdemo1.repository.FoodOptionRepository;

@Service
public class FoodOptionService {

    @Autowired
    private FoodOptionRepository foodOptionRepository;

    @Transactional
    public List<FoodOption> initializeFoodOptions() {
        List<FoodOption> options = Arrays.asList(
            // 1. 개수 단위 (Count Units) - type: 1
            FoodOption.builder().optionName("개").optionType(1).build(),
            FoodOption.builder().optionName("쪽").optionType(1).build(),
            FoodOption.builder().optionName("조각").optionType(1).build(),
            FoodOption.builder().optionName("알").optionType(1).build(),
            FoodOption.builder().optionName("마리").optionType(1).build(),
            FoodOption.builder().optionName("장").optionType(1).build(),
            FoodOption.builder().optionName("줄").optionType(1).build(),
            FoodOption.builder().optionName("뿌리").optionType(1).build(),
            FoodOption.builder().optionName("단").optionType(1).build(),
            
            // 2. 무게 단위 (Weight Units) - type: 2
            FoodOption.builder().optionName("g").optionType(2).build(),
            FoodOption.builder().optionName("kg").optionType(2).build(),
            FoodOption.builder().optionName("mg").optionType(2).build(),
            FoodOption.builder().optionName("근").optionType(2).build(),
            
            // 3. 부피 단위 (Volume Units) - type: 3
            FoodOption.builder().optionName("ml").optionType(3).build(),
            FoodOption.builder().optionName("L").optionType(3).build(),
            FoodOption.builder().optionName("cc").optionType(3).build(),
            FoodOption.builder().optionName("종이컵").optionType(3).build(),
            FoodOption.builder().optionName("컵").optionType(3).build(),
            FoodOption.builder().optionName("밥그릇").optionType(3).build(),
            
            // 4. 숟가락 단위 (Spoon Units) - type: 4
            FoodOption.builder().optionName("티스푼").optionType(4).build(),
            FoodOption.builder().optionName("테이블스푼").optionType(4).build(),
            FoodOption.builder().optionName("작은술").optionType(4).build(),
            FoodOption.builder().optionName("큰술").optionType(4).build(),
            FoodOption.builder().optionName("술").optionType(4).build(),
            
            // 5. 손으로 가늠하는 단위 (Hand-Measured Units) - type: 5
            FoodOption.builder().optionName("꼬집").optionType(5).build(),
            FoodOption.builder().optionName("줌").optionType(5).build(),
            FoodOption.builder().optionName("주먹").optionType(5).build(),
            FoodOption.builder().optionName("손바닥").optionType(5).build(),
            
            // 6. 기타 단위 (Other Units) - type: 6
            FoodOption.builder().optionName("방울").optionType(6).build(),
            FoodOption.builder().optionName("톨").optionType(6).build(),
            FoodOption.builder().optionName("포기").optionType(6).build(),
            FoodOption.builder().optionName("봉지").optionType(6).build(),
            FoodOption.builder().optionName("통").optionType(6).build(),
            FoodOption.builder().optionName("덩이").optionType(6).build(),
            FoodOption.builder().optionName("적당량").optionType(6).build()
        );

        return foodOptionRepository.saveAll(options);
    }
}
