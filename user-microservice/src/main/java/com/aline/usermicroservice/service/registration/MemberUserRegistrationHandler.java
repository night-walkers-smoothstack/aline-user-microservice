package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.NotFoundException;
import com.aline.core.model.Member;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.UserRole;
import com.aline.core.repository.MemberUserRepository;
import com.aline.usermicroservice.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.spi.PropertyMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberUserRegistrationHandler implements UserRegistrationHandler<MemberUser, MemberUserRegistration> {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Class<MemberUserRegistration> registersAs() {
        return MemberUserRegistration.class;
    }

    @Transactional(rollbackOn = NotFoundException.class)
    @Override
    public MemberUser register(MemberUserRegistration registration) {
        log.info("Register MEMBER USER with username: {}, password: {}, membership number: {}",
                registration.getUsername(),
                registration.getPassword(),
                registration.getMembershipId());
        Member member = memberService.getMemberByMembershipId(registration.getMembershipId());
        String hashedPassword = passwordEncoder.encode(registration.getPassword());
        return MemberUser.builder()
                .username(registration.getUsername())
                .password(hashedPassword)
                .member(member)
                .build();
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
