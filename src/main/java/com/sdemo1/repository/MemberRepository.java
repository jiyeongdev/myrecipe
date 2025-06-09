package com.sdemo1.repository;

import com.sdemo1.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByUserLoginId(String userLoginId);
    Optional<Member> findByEmail(String email);
}
