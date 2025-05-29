package com.sdemo1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sdemo1.entity.FoodOption;
import com.sdemo1.service.FoodOptionService;

@RestController
@RequestMapping("${api.base-path}/api/food-options")
public class FoodOptionController {

    private final FoodOptionService foodOptionService;

    @Autowired
    public FoodOptionController(FoodOptionService foodOptionService) {
        this.foodOptionService = foodOptionService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<List<FoodOption>> initializeFoodOptions() {
        try {
            List<FoodOption> savedOptions = foodOptionService.initializeFoodOptions();
            return ResponseEntity.ok(savedOptions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
