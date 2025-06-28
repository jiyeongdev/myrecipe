package com.sdemo1.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodIngredientResponse {
    private Integer fInId;
    private Integer memberId;
    private Integer foodId;
    private String foodName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
} 