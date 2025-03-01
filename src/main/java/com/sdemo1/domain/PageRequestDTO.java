package com.sdemo1.domain;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageRequestDTO {
    private int page; // 페이지 번호
    private int size; // 페이지 크기

    public static PageRequestDTO getDefault() {
        PageRequestDTO dto = new PageRequestDTO();
        dto.setPage(0); // 기본 페이지 번호
        dto.setSize(10); // 기본 페이지 크기
        return dto;
    }

    public Pageable toPageable() {
        return PageRequest.of(page, size);
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