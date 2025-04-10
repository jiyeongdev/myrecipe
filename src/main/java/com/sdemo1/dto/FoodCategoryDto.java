package com.sdemo1.dto;

import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FoodCategoryDto {
    private String MID;  // Main Category ID
    private String SID;  // Sub Category ID
    private PageRequestDto pageRequest = PageRequestDto.getDefault();
    private String categoryId;
    private String categoryName;
    private String parentCategoryId;


    public static FoodCategoryDto from(Map<String, String> params) {
        FoodCategoryDto dto = new FoodCategoryDto();
        dto.setMID((String)params.get("mID"));
        dto.setSID((String)params.get("sID"));
        if (!params.containsKey("page") && !params.containsKey("size")) {
            dto.setPageRequest((PageRequestDto)null);
        } else {
            Integer page = params.containsKey("page") ? Integer.parseInt((String)params.get("page")) : null;
            Integer size = params.containsKey("size") ? Integer.parseInt((String)params.get("size")) : null;
            dto.setPageRequest(new PageRequestDto(page, size));
        }

        return dto;
    }
}
