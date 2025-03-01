package com.sdemo1.repository;

import java.util.List;
import java.util.Optional;

import com.sdemo1.entity.Member;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByName(String name);
    List<Member> findAll();

}
