package com.sdemo1.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestDto{
    private int page;
    private int size;

    public PageRequestDto(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public static PageRequestDto getDefault() {
        return new PageRequestDto(0, 10);
    }

    public Pageable toPageable() {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "foodID"));
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}