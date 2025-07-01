package com.sdemo1.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.sdemo1.request.CookRecipeRequest;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookRecipeResponse {
    private Integer cookId;
    private Integer userId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userName; // 사용자 닉네임
    private String cookTitle;
    private String cookImg;
    private List<CookRecipeRequest.Ingredient> ingredients;
    private List<CookRecipeRequest.RecipeStepDetail> recipeSteps;
}
