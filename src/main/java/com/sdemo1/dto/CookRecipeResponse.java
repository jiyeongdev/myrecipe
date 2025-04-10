package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookRecipeResponse {
    private Integer cookId;
    private Integer userId;
    private String cookTitle;
    private String cookImg;
    private String cookIngredient;
}
