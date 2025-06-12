package com.sdemo1.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private Long memberId;
    private String name;
    private String role;
    private String phone;
    private Boolean completeFlag;
} 