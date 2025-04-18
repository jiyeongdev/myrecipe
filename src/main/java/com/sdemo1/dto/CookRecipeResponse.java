package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookRecipeResponse {
    private Integer cookId;
    private Integer userId;
    private String cookTitle;
    private String cookImg;
    private List<CookRecipeRequest.Ingredient> Ingredients;
    private List<CookRecipeRequest.RecipeStepDetail> recipeSteps;
}
