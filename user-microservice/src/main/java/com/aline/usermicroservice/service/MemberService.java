package com.aline.usermicroservice.service;

import com.aline.core.exception.notfound.MemberNotFoundException;
import com.aline.core.model.Member;
import com.aline.core.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository repository;

    public Member getMemberByMembershipId(String membershipId) {
        return repository.findByMembershipId(membershipId).orElseThrow(MemberNotFoundException::new);
    }
}
