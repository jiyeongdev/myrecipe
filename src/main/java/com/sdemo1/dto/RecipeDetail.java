package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Redis에 저장되는 레시피 상세 정보 (공통 캐시)
 * key: "recipe_detail:{cookId}"
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetail {
    
    private Integer cookId;         // 레시피 ID
    private String cookTitle;       // 레시피 제목
    private Integer authorId;       // 작성자 ID
    private String cookImg;         // 레시피 이미지 URL
    private String authorName;      // 작성자 이름 (나중에 조인으로 추가 가능)
    
    /**
     * 작성자 표시용 텍스트
     * @return "홍길동님의 레시피"
     */
    public String getAuthorDisplayText() {
        String name = authorName != null ? authorName : "사용자" + authorId;
        return name + "님의 레시피";
    }
} 