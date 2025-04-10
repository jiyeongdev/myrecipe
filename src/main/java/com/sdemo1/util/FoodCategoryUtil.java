package com.sdemo1.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sdemo1.entity.FoodItem;

public class FoodCategoryUtil {
    private static Map<String, Map<String, Object>> categoryMap = null;

    public static Map<String, Map<String, Object>> getCategoryMap(List<FoodItem> foodItems) {
        if (categoryMap == null) {
            synchronized (FoodCategoryUtil.class) {
                if (categoryMap == null) {
                    categoryMap = createCategoryMap(foodItems);
                }
            }
        }
        return categoryMap;
    }

    private static Map<String, Map<String, Object>> createCategoryMap(List<FoodItem> foodItems) {
        // 메인 카테고리 맵 생성
        Map<String, Map<String, Object>> mainCategoryMap = foodItems.stream()
                .filter(obj -> "P".equals(obj.getParentID()) || "R".equals(obj.getParentID()))
                .collect(Collectors.toMap(
                    item -> String.valueOf(item.getFoodID()),
                    main -> {
                        Map<String, Object> mainInfo = new HashMap<>();
                        mainInfo.put("mID", main.getFoodID());
                        mainInfo.put("mName", main.getFoodName());
                        return mainInfo;
                    }
                ));

        // 서브 카테고리 맵 생성
        return foodItems.stream()
                .filter(obj -> !("P".equals(obj.getParentID()) || "R".equals(obj.getParentID())))
                .collect(Collectors.toMap(
                    item -> String.valueOf(item.getFoodID()),
                    sub -> {
                        Map<String, Object> subInfo = new HashMap<>();
                        subInfo.put("sName", sub.getFoodName());
                        Map<String, Object> mainInfo = mainCategoryMap.get(String.valueOf(sub.getParentID()));
                        if (mainInfo != null) {
                            subInfo.put("mID", mainInfo.get("mID"));
                            subInfo.put("mName", mainInfo.get("mName"));
                        }
                        return subInfo;
                    }
                ));
    }

    public static void clearCache() {
        categoryMap = null;
    }
} 