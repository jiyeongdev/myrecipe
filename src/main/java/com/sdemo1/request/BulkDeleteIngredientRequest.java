package com.sdemo1.request;

import lombok.Data;
import java.util.List;

@Data
public class BulkDeleteIngredientRequest {
    private List<Integer> foodIds;
} 