package com.sdemo1.repository;

import com.sdemo1.entity.MemberSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberSocialAccountRepository extends JpaRepository<MemberSocialAccount, Integer> {
    Optional<MemberSocialAccount> findByUserLoginId(String userLoginId);
    Optional<MemberSocialAccount> findByEmail(String email);
    Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialAccount.Provider provider, String providerId);
} 