package com.aline.usermicroservice.service;

import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.BadRequestException;
import com.aline.core.exception.ConflictException;
import com.aline.core.model.Member;
import com.aline.core.model.user.MemberUser;
import com.aline.core.repository.MemberUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class MemberUserService {
    private final MemberService memberService;
    private final MemberUserRepository repository;

    public boolean memberUserExists(String membershipId) {
        return repository.existsByMembershipId(membershipId);
    }

    /**
     * Register a new MemberUser (A user entity with a Member attached to it.)
     * @param userRegistration The User Registration DTO
     * @return A UserResponse
     */
    public UserResponse registerMemberUser(@Valid MemberUserRegistration userRegistration) {
        if (memberUserExists(userRegistration.getMembershipId())) {

            Member member = memberService.getMemberByMembershipId(userRegistration.getMembershipId());
            MemberUser user = MemberUser.builder()
                    .member(member)
                    .username(userRegistration.getUsername())
                    .build();

        }
        throw new ConflictException("User already exists for this member.");
    }
}
