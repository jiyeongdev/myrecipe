package com.sdemo1.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sdemo1.entity.Member;
import com.sdemo1.repository.MemberRepository;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public int createMember() {
        Member member = new Member();
        member.setUserName("");
        member.setUserLoginId("");
        member.setUserLoginPw("");
        Member savedMember = memberRepository.save(member);
        return savedMember.getUserId();
    }

    public int join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getUserId();
    }

    private void validateDuplicateMember(Member member) {
        memberRepository.findByUserLoginId(member.getUserLoginId())
            .ifPresent(m -> {
                throw new IllegalStateException("이미 존재하는 회원입니다.");
            });
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> findOneById(Integer memberId) {
        return memberRepository.findById(memberId);
    }
}
