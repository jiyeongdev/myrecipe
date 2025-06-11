package com.sdemo1.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sdemo1.repository.MemberSocialAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    

    private final MemberSocialAccountRepository memberSocialAccountRepository;

    /**
     * Spring Security가 사용자 인증을 할 때 사용자 정보를 로드하는 역할
     * loadUserByUsername 메서드는 로그인 시도 시 호출됨
     * 
     * 사용 시점:
     * 일반 로그인 시도할 때
     * JWT 토큰 검증 시
     * Spring Security의 인증 필터에서 사용자 정보가 필요할 때

     * @param username 사용자 이름
     * @return 사용자 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUserDetails data = memberSocialAccountRepository.findByUserLoginId(username)
                .map(account -> new CustomUserDetails(account.getMember(), account))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        log.info("사용자 정보: {}", data);
        return data;
    }
} 