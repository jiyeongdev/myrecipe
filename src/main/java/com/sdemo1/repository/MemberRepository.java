package com.sdemo1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sdemo1.entity.Member;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByUserLoginId(String userLoginId);
}
