package com.aline.usermicroservice.service.function;

import com.aline.core.model.user.User;

/**
 * Intercept the persisted user entity before it is
 * converted into a UserResponse DTO.
 */
@FunctionalInterface
public interface UserRegistrationConsumer {
    void onRegistrationComplete(User user);
}
