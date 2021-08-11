package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.AdminUser;
import com.aline.core.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementation of the UserRegistrationHandler interface.
 * This class provides registration logic specifically for
 * the AdminUser entity.
 */
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
