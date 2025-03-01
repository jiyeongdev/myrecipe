package com.sdemo1.repository;

import java.util.*;

import com.sdemo1.entity.Member;

public class MemoryMemberRepository implements MemberRepository{

    // 동시성 문제가 있어 공유되는 변수인 경우 hashmap 말고 ConcurrentHashMap 을 써야되는데 예제이니 그냥 쓴다
    private static Map<Long, Member> store = new HashMap<>();

    //동시성 문제로 AtomicLong 사용해야하는데 예제이니그냥 쓴다
    private static long sequence = 0L;

    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId() , member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        return store.values().stream()
                .filter(member-> member.getName().equals(name))
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore(){
        store.clear();
    }
}
