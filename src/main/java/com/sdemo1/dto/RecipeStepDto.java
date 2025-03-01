package com.sdemo1.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

// lombok
@Data
@NoArgsConstructor // 기본 생성자(public RecipeStepDto() {})를 자동 생성합니다.
@AllArgsConstructor // 모든 필드를 포함하는 생성자(public RecipeStepDto(String description, File photoFile))를 자동 생성합니다.
public class RecipeStepDto {
    @NotNull
//    @Size(min = 1, message = "Description cannot be empty")
    private String description;

}