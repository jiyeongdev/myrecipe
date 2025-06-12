package com.sdemo1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sdemo1.entity.MemberSocialAccount;
import com.sdemo1.repository.MemberSocialAccountRepository;

import java.util.Optional;

@Service
@Transactional
public class MemberSocialAccountService {
    private final MemberSocialAccountRepository memberSocialAccountRepository;

    public MemberSocialAccountService(MemberSocialAccountRepository memberSocialAccountRepository) {
        this.memberSocialAccountRepository = memberSocialAccountRepository;
    }

    /**
     * 소셜 계정 생성
     */
    public MemberSocialAccount createSocialAccount(Integer memberId, String email, 
            MemberSocialAccount.Provider provider, String providerId, String profileImg) {
        String userLoginId = generateLoginId(email, provider, providerId);
        validateDuplicateUserLoginId(userLoginId);
        
        MemberSocialAccount socialAccount = new MemberSocialAccount();
        socialAccount.setMemberId(memberId);
        socialAccount.setUserLoginId(userLoginId);
        socialAccount.setEmail(email);
        socialAccount.setProvider(provider);
        socialAccount.setProviderId(providerId);
        socialAccount.setProfileImg(profileImg);
        socialAccount.setUserLoginPw("OAUTH2_USER"); // 소셜 로그인의 경우 임시 비밀번호 설정
        
        return memberSocialAccountRepository.save(socialAccount);
    }

    /**
     * 로그인 ID 생성
     * - 이메일이 있는 경우: 이메일 사용
     * - 이메일이 없는 경우: "provider_providerId" 형식 사용
     */
    private String generateLoginId(String email, MemberSocialAccount.Provider provider, String providerId) {
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        return provider.name().toLowerCase() + "_" + providerId;
    }

    public Optional<MemberSocialAccount> findByUserLoginId(String userLoginId) {
        return memberSocialAccountRepository.findByUserLoginId(userLoginId);
    }

    public Optional<MemberSocialAccount> findByEmail(String email) {
        return memberSocialAccountRepository.findByEmail(email);
    }

    public Optional<MemberSocialAccount> findByProviderAndProviderId(
            MemberSocialAccount.Provider provider, String providerId) {
        return memberSocialAccountRepository.findByProviderAndProviderId(provider, providerId);
    }

    private void validateDuplicateUserLoginId(String userLoginId) {
        memberSocialAccountRepository.findByUserLoginId(userLoginId)
            .ifPresent(m -> {
                throw new IllegalStateException("이미 존재하는 회원입니다.");
            });
    }
} 