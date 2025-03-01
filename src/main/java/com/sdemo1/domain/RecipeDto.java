package com.sdemo1.domain;

import java.util.List;

public class RecipeDto {
    private String title; // 요리 이름
    private String userID; // 작성자 이름
    private List<RecipeStepDto> steps; // 단계별 설명 및 사진 리스트

    // 기본 생성자
    public RecipeDto() {}

    // 생성자
    public RecipeDto(String title, String userID, List<RecipeStepDto> steps) {
        this.title = title;
        this.userID = userID;
        this.steps = steps;
    }

    // Getter와 Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public List<RecipeStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<RecipeStepDto> steps) {
        this.steps = steps;
    }

    // toString 메서드
    @Override
    public String toString() {
        return "RecipeDto{" +
                "title='" + title + '\'' +
                ", userID='" + userID + '\'' +
                ", steps=" + steps +
                '}';
    }
}

