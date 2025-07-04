package com.sdemo1.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.request.CookRecipeRequest;
import com.sdemo1.response.CookRecipeResponse;
import com.sdemo1.service.CookRecipeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cook/recipes")
@RequiredArgsConstructor
public class CookRecipeController {

    private final CookRecipeService cookRecipeService;

    @PostMapping
    public ResponseEntity<Integer> createRecipe(@RequestBody CookRecipeRequest request) {
        try {
            Integer cookId = cookRecipeService.createRecipe(request);
            return ResponseEntity.ok(cookId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<CookRecipeResponse>> getRecipesByUserId(@PathVariable("userId") Integer userId) {
        try {
            List<CookRecipeResponse> recipes = cookRecipeService.getRecipesByUserId(userId);
            return new ApiResponse<>(
                null,
                recipes,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/detail/{cookId}")
    public ApiResponse<List<CookRecipeResponse>> getRecipesByCookID(@PathVariable("cookId") Integer cookId) {
        try {
            List<CookRecipeResponse> recipes = cookRecipeService.getRecipesByCookId(cookId);
            return new ApiResponse<>(
                null,
                recipes,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/{cookId}")
    public ApiResponse<Void> updateRecipe(
            @PathVariable("cookId")  int cookId,
            @RequestBody CookRecipeRequest request) {
        try {
            cookRecipeService.updateRecipe(cookId, request);
            return new ApiResponse<>(
                "레시피가 성공적으로 업데이트되었습니다.",
                null,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                "레시피 업데이트 실패하였습니다: " + e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * 레시피 삭제 API
     * @param cookId 삭제할 레시피 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{cookId}")
    public ApiResponse<Void> deleteRecipe(@PathVariable("cookId") int cookId) {
        try {
            cookRecipeService.deleteRecipe(cookId);
            return new ApiResponse<>(
                "레시피가 성공적으로 삭제되었습니다.",
                null,
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                "레시피 삭제 실패하였습니다: " + e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
            );
        }
    }
}