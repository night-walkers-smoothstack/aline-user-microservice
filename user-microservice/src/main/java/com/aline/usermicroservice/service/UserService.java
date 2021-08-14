package com.aline.usermicroservice.service;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.usermicroservice.service.function.UserRegistrationConsumer;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

/**
 * User service interface that provides basic CRUD method implementation.
 */
public interface UserService {
    UserResponse getUserById(Long id);
    PaginatedResponse<UserResponse> getAllUsers(Pageable pageable, String search);
    UserResponse registerUser(UserRegistration registration);
    UserResponse registerUser(UserRegistration registration, @Nullable UserRegistrationConsumer consumer);
    void enableUser(Long id);
    User getUserByToken(UserRegistrationToken uuid);
}
