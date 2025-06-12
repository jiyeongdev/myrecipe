package com.sdemo1.request;

import java.util.List;
import lombok.Data;

@Data
public class CookRecipeRequest {
    private Integer userId;
    private String cookTitle;
    private String cookImg;
    private List<Ingredient> ingredients;
    private List<RecipeStepDetail> recipeSteps;

    @Data
    public static class Ingredient {
        private String unit;
        private Integer foodId;
        private Integer unitId;
        private String quantity;
        private String foodName;
        private String isRequired;
    }

    @Data
    public static class RecipeStepDetail {
        private String description;
        private String img;
    }
}
