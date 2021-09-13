package com.aline.usermicroservice.service;

import com.aline.core.exception.notfound.MemberNotFoundException;
import com.aline.core.model.Member;
import com.aline.core.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Member Service
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository repository;

    /**
     * Get member by their membership ID
     * @param membershipId the provided ID to query
     * @return A member with the provided membership ID
     */
    public Member getMemberByMembershipId(String membershipId) {
        return repository.findByMembershipId(membershipId).orElseThrow(MemberNotFoundException::new);
    }

    // Save member
    public void saveMember(Member member) {
        repository.save(member);
    }
}
