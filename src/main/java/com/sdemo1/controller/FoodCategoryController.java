package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.FoodCategoryResponse;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.exception.CustomException;
import com.sdemo1.service.FoodService;
import com.sdemo1.common.utils.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/food")
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
    public ApiResponse<FoodCategoryResponse> findFoodCategory() {
        List<FoodItem> foodItems = foodService.findFoodCategory();
        
        // 서브 카테고리 맵 생성
        Map<String, List<FoodItem>> subMapCategory = foodItems.stream()
                .filter(obj -> !("P".equals(obj.getParentID()) || "R".equals(obj.getParentID())))
                .collect(Collectors.groupingBy(FoodItem::getParentID));

        // 메인 카테고리 리스트 생성
        List<FoodItem> mainMapCategory = foodItems.stream()
                .filter(obj -> "P".equals(obj.getParentID()) || "R".equals(obj.getParentID()))
                .collect(Collectors.toList()); // List<FoodItem>으로 변환

        // FoodCategoryResponse 생성
        FoodCategoryResponse foodCategoryData = new FoodCategoryResponse(mainMapCategory, subMapCategory);

        return new ApiResponse<>(true, "성공", foodCategoryData, HttpStatus.OK);
    }

    @GetMapping("/ingredient")
    public ApiResponse<?> findIngredient(@RequestParam Map<String, String> cIDs) {
        if (!ValidateUtils.isValidParam(cIDs.get("mID")) || !ValidateUtils.isValidParam(cIDs.get("sID"))) {
            throw new CustomException("mID 또는 sID 키는 존재하나 값이 비어있습니다.", HttpStatus.BAD_REQUEST.value());
        }

        return new ApiResponse<>(true, "성공", foodService.findIngredientByID(cIDs), HttpStatus.OK);
    }

    @GetMapping("/ingredient-all")
    public ApiResponse<?> allIngredient(@RequestParam Integer lastID, @RequestParam(defaultValue = "10") int size) {
        if (lastID == null) {
            lastID = 0;
        }
        return new ApiResponse<>(true, "성공", foodService.allIngredient(lastID, size), HttpStatus.OK);
    }

    @GetMapping("/sub-ingredient")
    public ApiResponse<List<FoodItem>> getSubIngredient(@RequestParam("parentID") String parentID){
        List<FoodItem> foodItems = foodService.GetByParentID(parentID);
        System.out.println("parentID : " + parentID +" 에 대한 하위분류 "+foodItems);
        return new ApiResponse<>(true, "성공", foodItems, HttpStatus.OK);
    }

    // 재료선택 화면 API
    @GetMapping("/findIngredientByFilter")
    public ApiResponse<?> findIngredientByFilter(@RequestParam Map<String, String> params){
        return new ApiResponse<>(true, "성공", foodService.findIngredientByFilter(params), HttpStatus.OK);
    }

} 