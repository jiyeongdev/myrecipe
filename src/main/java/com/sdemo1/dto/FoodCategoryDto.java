package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

// lombok
@Data
@AllArgsConstructor // 모든 필드를 포함하는 생성자(public RecipeStepDto(String description, File photoFile))를 자동 생성합니다.
@NoArgsConstructor // 기본 생성자 추가
public class FoodCategoryDto {
    private String mid; // 메인 카테고리 ID
    private String sid; // 서브 카테고리 ID

    public static FoodCategoryDto from(Map<String, String> params) {
        FoodCategoryDto dto = new FoodCategoryDto();
        dto.setMid(params.get("mid"));
        dto.setSid(params.get("sid"));
        return dto;
    }

    public String getMID() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getSID() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
