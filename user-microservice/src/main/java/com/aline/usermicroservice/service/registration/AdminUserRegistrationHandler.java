package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.conflict.EmailConflictException;
import com.aline.core.exception.conflict.UsernameConflictException;
import com.aline.core.model.user.AdminUser;
import com.aline.core.model.user.UserRole;
import com.aline.core.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * Implementation of the UserRegistrationHandler interface.
 * This class provides registration logic specifically for
 * the AdminUser entity.
 */
@Component
@RequiredArgsConstructor
public class AdminUserRegistrationHandler implements UserRegistrationHandler<AdminUser, AdminUserRegistration> {

    private final PasswordEncoder passwordEncoder;
    private final AdminUserRepository repository;

    @Override
    public Class<AdminUserRegistration> registersAs() {
        return AdminUserRegistration.class;
    }

    @Transactional(rollbackOn = {UsernameConflictException.class, EmailConflictException.class})
    @Override
    public AdminUser register(AdminUserRegistration registration) {
        if (repository.existsByUsername(registration.getUsername()))
            throw new UsernameConflictException();
        if (repository.existsByEmail(registration.getEmail()))
            throw new EmailConflictException();
        String hashedPassword = passwordEncoder.encode(registration.getPassword());
        AdminUser user = AdminUser.builder()
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .email(registration.getEmail())
                .username(registration.getUsername())
                .phone(registration.getPhone())
                .password(hashedPassword)
                .build();
        return repository.save(user);
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
