package com.sdemo1.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.FoodCategoryDto;
import com.sdemo1.dto.PageRequestDto;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.service.FoodService;

@RestController
@RequestMapping("${api.base-path}/food")
public class FoodCategoryController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/category")
    public ApiResponse<List<FoodItem>> getMainIngredient(){
        List<FoodItem> foodItems = foodService.findByParentIDIn(Arrays.asList("P","R"));
        System.out.println("대분류 : " + foodItems);
        return new ApiResponse<>(true, "성공", foodItems, HttpStatus.OK);
    }

    @GetMapping("/allCategory")
    public ApiResponse<List<Map<String, Object>>> findFoodCategory() {
        List<FoodItem> foodItems = foodService.findFoodCategory();
        
        // 서브 카테고리 맵 생성
        Map<String, List<Map<String, Object>>> subMapCategory = foodItems.stream()
                .filter(obj -> !("P".equals(obj.getParentID()) || "R".equals(obj.getParentID())))
                .collect(Collectors.groupingBy(
            FoodItem::getParentID,
            Collectors.mapping(
                item -> {
                    Map<String, Object> subCategory = new HashMap<>();
                    subCategory.put("sID", item.getFoodID());
                    subCategory.put("sName", item.getFoodName());
                    // ID 필드는 제외하고 필요한 필드만 추가
                    return subCategory;
                },
                Collectors.toList()
            )
        ));

        // 메인 카테고리 리스트 생성 및 서브카테고리 매핑
        List<Map<String, Object>> result = foodItems.stream()
                .filter(obj -> "P".equals(obj.getParentID()) || "R".equals(obj.getParentID()))
                .map(main -> {
                    Map<String, Object> categoryMap = new HashMap<>();
                    categoryMap.put("mID", main.getFoodID());
                    categoryMap.put("mName", main.getFoodName());
                    categoryMap.put("sList", subMapCategory.getOrDefault(String.valueOf(main.getFoodID()), Collections.emptyList()));
                    return categoryMap;
                })
                .collect(Collectors.toList());

        return new ApiResponse<>(true, "성공", result, HttpStatus.OK);
    }

    @GetMapping("/sub-ingredient") //사용하진 않으나 그냥 테스트용
    public ApiResponse<List<FoodItem>> getSubIngredient(@RequestParam("parentID") String parentID){
        List<FoodItem> foodItems = foodService.GetByParentID(parentID);
        System.out.println("parentID : " + parentID +" 에 대한 하위분류 "+foodItems);
        return new ApiResponse<>(true, "성공", foodItems, HttpStatus.OK);
    }

    // 재료선택 화면 API
    @GetMapping("/findIngredientByFilter")
    public ApiResponse<?> findIngredientByFilter(
            @ModelAttribute FoodCategoryDto params,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        
        params.setPageRequest(new PageRequestDto(page, size));
        return new ApiResponse<>(true, "성공", foodService.findIngredientByFilter(params), HttpStatus.OK);
    }

    @GetMapping("/search/{keyword}")
    public ApiResponse<Page<FoodItem>> findByFoodName(
            @PathVariable String keyword,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        
        PageRequestDto pageRequest = new PageRequestDto(page, size);
        Page<FoodItem> foodItems = foodService.findByFoodName(keyword, pageRequest);
        return new ApiResponse<>(true, "성공", foodItems, HttpStatus.OK);
    }
} 
