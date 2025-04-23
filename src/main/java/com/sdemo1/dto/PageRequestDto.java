package com.sdemo1.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class PageRequestDto {
    private Integer page;
    private Integer size;

    public static PageRequestDto getDefault() {
        return new PageRequestDto(1, 10);
    }

    public PageRequestDto() {
        this.page = 1;
        this.size = 10;
    }

    public PageRequestDto(Integer page, Integer size) {
        this.page = page != null ? page : 1;
        this.size = size != null ? size : 10;
    }

    public Pageable toPageable() {
        if (page == null && size == null) {
            return Pageable.unpaged();
        }
        int pageSize = (size == null) ? Integer.MAX_VALUE : size;
        return PageRequest.of(validatePage(page) - 1, pageSize, Sort.by(Sort.Direction.ASC, "foodID"));
    }

    private int validatePage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }
}