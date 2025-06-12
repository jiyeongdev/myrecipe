package com.sdemo1.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sdemo1.repository.MemberSocialAccountRepository;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.entity.Member;
import com.sdemo1.entity.MemberSocialAccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    

    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final MemberRepository memberRepository;

    /**
     * Spring Security가 사용자 인증을 할 때 사용자 정보를 로드하는 역할
     * loadUserByUsername 메서드는 로그인 시도 시 호출됨
     * 
     * 사용 시점:
     * 일반 로그인 시도할 때
     * JWT 토큰 검증 시
     * Spring Security의 인증 필터에서 사용자 정보가 필요할 때

     * @param memberId 사용자 ID
     * @return 사용자 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        // memberId로 Member를 직접 조회
        Member member = memberRepository.findById(Integer.parseInt(memberId))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + memberId));
        
        // 해당 member의 소셜 계정 중 첫 번째 계정을 사용
        MemberSocialAccount socialAccount = memberSocialAccountRepository.findByMemberId(member.getMemberId())
                .orElseThrow(() -> new UsernameNotFoundException("소셜 계정을 찾을 수 없습니다: " + memberId));
        
        log.info("사용자 정보: memberId={}, name={}", member.getMemberId(), member.getName());
        return new CustomUserDetails(member, socialAccount);
    }
} 