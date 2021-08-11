package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminUserRegistrationHandler implements UserRegistrationHandler<AdminUserRegistration> {
    @Override
    public Class<AdminUserRegistration> registersAs() {
        return AdminUserRegistration.class;
    }

    @Override
    public UserResponse register(AdminUserRegistration registration) {
        log.info("Register ADMIN USER with username: {}, password: {}, firstName: {}, lastName: {}, email: {}",
                registration.getUsername(),
                registration.getPassword(),
                registration.getFirstName(),
                registration.getLastName(),
                registration.getEmail());
        return null;
    }
}
