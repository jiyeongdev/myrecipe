package com.sdemo1.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestDto {
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
        if (page == null && size == null) {
            return Pageable.unpaged();
        }
        int pageSize = (size == null) ? Integer.MAX_VALUE : size;
        return PageRequest.of(validatePage(page) - 1, pageSize, Sort.by(Sort.Direction.ASC, "foodID"));
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
        return (page == null || page < 1) ? 1 : page;
    }
}