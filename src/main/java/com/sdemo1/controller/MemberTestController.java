package com.sdemo1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.service.MemberService;

@RestController
@RequestMapping("/api/proxy/member")
public class MemberTestController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/create/id")
    public ApiResponse<Integer> createMemberId() {
        int userId = memberService.createMember();
        return new ApiResponse<>( "성공", userId, HttpStatus.OK);
    }
} 
