package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.AdminUser;
import com.aline.core.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserRegistrationHandler implements UserRegistrationHandler<AdminUser, AdminUserRegistration> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public Class<AdminUserRegistration> registersAs() {
        return AdminUserRegistration.class;
    }

    @Override
    public AdminUser register(AdminUserRegistration registration) {
        log.info("Register ADMIN USER with username: {}, password: {}, firstName: {}, lastName: {}, email: {}",
                registration.getUsername(),
                registration.getPassword(),
                registration.getFirstName(),
                registration.getLastName(),
                registration.getEmail());
        String hashedPassword = passwordEncoder.encode(registration.getPassword());
        return AdminUser.builder()
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .email(registration.getEmail())
                .username(registration.getUsername())
                .password(hashedPassword)
                .build();
    }

    @Override
    public UserResponse mapToResponse(AdminUser adminUser) {
        return UserResponse.builder()
                .id(adminUser.getId())
                .firstName(adminUser.getFirstName())
                .lastName(adminUser.getLastName())
                .username(adminUser.getUsername())
                .email(adminUser.getEmail())
                .role(UserRole.valueOf(adminUser.getRole().toUpperCase()))
                .enabled(adminUser.isEnabled())
                .build();
    }
}
