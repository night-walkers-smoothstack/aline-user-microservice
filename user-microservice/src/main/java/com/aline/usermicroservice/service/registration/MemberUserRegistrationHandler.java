package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.repository.MemberUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberUserRegistrationHandler implements UserRegistrationHandler<MemberUserRegistration> {

    @Override
    public Class<MemberUserRegistration> registersAs() {
        return MemberUserRegistration.class;
    }

    @Override
    public UserResponse register(MemberUserRegistration registration) {
        log.info("Register MEMBER USER with username: {}, password: {}, membership number: {}",
                registration.getUsername(),
                registration.getPassword(),
                registration.getMembershipId());
        return null;
    }
}
