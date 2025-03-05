package com.sdemo1.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestDto{
    private Integer page;
    private Integer size;

    public PageRequestDto(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }

    public static PageRequestDto getDefault() {
        return new PageRequestDto(1, 10);
    }

    public Pageable toPageable() {
        // page와 size 모두 null이면 페이징 없이 전체 데이터 조회
        if (page == null && size == null) {
            return Pageable.unpaged();
        }
     
        return PageRequest.of(validatePage(page) - 1,size, Sort.by(Sort.Direction.ASC, "foodID"));
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    private int validatePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }
}