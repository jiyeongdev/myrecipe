package com.sdemo1.controller;

import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.MemberProfileRequest;
import com.sdemo1.dto.MemberResponse;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.entity.Member;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/info")
    public ApiResponse<MemberResponse> getMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName();
        
        Optional<Member> memberOptional = memberRepository.findById(Integer.parseInt(memberId));
        if (!memberOptional.isPresent()) {
            return new ApiResponse<>("사용자를 찾을 수 없습니다.", null, HttpStatus.NOT_FOUND);
        }
        Member member = memberOptional.get();
        
        MemberResponse response = MemberResponse.builder()
            .memberId(Long.parseLong(memberId))
            .role(auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
            .completeFlag(member.getCompleteFlag())
            .name(member.getName())
            .phone(member.getPhone())
            .build();
        
        return new ApiResponse<>("사용자 정보 조회 성공", response, HttpStatus.OK);
    }

    @PostMapping("/profile")
    public ApiResponse<MemberResponse> updateMember(@RequestBody MemberProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName();

        Optional<Member> memberOptional = memberRepository.findById(Integer.parseInt(memberId));
        if (!memberOptional.isPresent()) {
            return new ApiResponse<>("사용자를 찾을 수 없습니다.", null, HttpStatus.NOT_FOUND);
        }
        Member member = memberOptional.get();

        // 전화번호에서 숫자 이외의 문자(특수문자, 공백 등)를 모두 제거합니다.
        String sanitizedPhone = request.getPhone().replaceAll("[^\\d]", "");

        // 전화번호 중복 체크 (현재 사용자의 전화번호가 아닌 경우에만 체크)
        if (!sanitizedPhone.equals(member.getPhone())) {
            if (memberRepository.findByPhone(sanitizedPhone).isPresent()) {
                return new ApiResponse<>("이미 사용 중인 전화번호입니다.", null, HttpStatus.CONFLICT);
            }
        }

        member.setName(request.getName());
        member.setPhone(sanitizedPhone);
        member.setCompleteFlag(true);
        memberRepository.save(member);
        return new ApiResponse<>("사용자 정보 업데이트 성공", null, HttpStatus.OK);
    }
} 