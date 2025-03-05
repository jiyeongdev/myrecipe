package com.sdemo1.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// lombok
@Data
@AllArgsConstructor // 모든 필드를 포함하는 생성자(public RecipeStepDto(String description, File photoFile))를 자동 생성합니다.
@NoArgsConstructor // 기본 생성자 추가
public class FoodCategoryDto {
    private String mID; // 메인 카테고리 ID
    private String sID; // 서브 카테고리 ID
    private PageRequestDto pageRequest; // 페이지네이션 정보

    public static FoodCategoryDto from(Map<String, String> params) {
        FoodCategoryDto dto = new FoodCategoryDto();
        dto.setMID(params.get("mID"));
        dto.setSID(params.get("sID"));
            
        // page나 size 파라미터가 없으면 페이징 없이 전체 조회
        if (!params.containsKey("page") && !params.containsKey("size")) {
            dto.setPageRequest(null);
        } else {
            Integer page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : null;
            Integer size = params.containsKey("size") ? Integer.parseInt(params.get("size")) : null;
            dto.setPageRequest(new PageRequestDto(page, size));
        }
        
        return dto;
    }

    public String getMID() {
        return mID;
    }

    public void setMID(String mID) {
        this.mID = mID;
    }

    public String getSID() {
        return sID;
    }

    public void setSID(String sID) {
        this.sID = sID;
    }

    public PageRequestDto getPageRequest() {
        if (pageRequest.getPage() == null && pageRequest.getSize() == null) { //페이징 없이 전체 조회
            return new PageRequestDto(null, null);
        }
        return pageRequest;
    }

    public void setPageRequest(PageRequestDto pageRequest) {
        this.pageRequest = pageRequest;
    }
}
