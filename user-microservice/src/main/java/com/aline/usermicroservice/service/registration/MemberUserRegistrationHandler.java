package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.ConflictException;
import com.aline.core.exception.UnprocessableException;
import com.aline.core.exception.conflict.UsernameConflictException;
import com.aline.core.exception.notfound.MemberNotFoundException;
import com.aline.core.model.Member;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.UserRole;
import com.aline.core.repository.MemberUserRepository;
import com.aline.usermicroservice.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * Implementation of the UserRegistrationHandler interface.
 * This class provides an implementation that registers specifically
 * a MemberUser entity.
 */
@Component
@RequiredArgsConstructor
public class MemberUserRegistrationHandler implements UserRegistrationHandler<MemberUser, MemberUserRegistration> {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MemberUserRepository repository;

    @Override
    public Class<MemberUserRegistration> registersAs() {
        return MemberUserRegistration.class;
    }

    @Transactional(rollbackOn = {MemberNotFoundException.class, ConflictException.class, UsernameConflictException.class, UnprocessableException.class})
    @Override
    public MemberUser register(MemberUserRegistration registration) {
        if (repository.existsByUsername(registration.getUsername()))
            throw new UsernameConflictException();
        Member member = memberService.getMemberByMembershipId(registration.getMembershipId());
        if (!member.getApplicant().getSocialSecurity().endsWith(registration.getLastFourOfSSN()))
            throw new MemberNotFoundException();
        if (repository.existsByMembershipId(registration.getMembershipId()))
            throw new ConflictException("A user already exists with this membership.");
        String hashedPassword = passwordEncoder.encode(registration.getPassword());
        MemberUser user = MemberUser.builder()
                .username(registration.getUsername())
                .password(hashedPassword)
                .member(member)
                .build();

        return repository.save(user);
    }

    @Override
    public UserResponse mapToResponse(MemberUser memberUser) {
        return UserResponse.builder()
                .id(memberUser.getId())
                .firstName(memberUser.getMember().getApplicant().getFirstName())
                .lastName(memberUser.getMember().getApplicant().getLastName())
                .username(memberUser.getUsername())
                .email(memberUser.getMember().getApplicant().getEmail())
                .role(UserRole.valueOf(memberUser.getRole().toUpperCase()))
                .enabled(memberUser.isEnabled())
                .build();
    }
}
