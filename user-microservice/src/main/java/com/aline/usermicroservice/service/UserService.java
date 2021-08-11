package com.aline.usermicroservice.service;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getUserById(Long id);
    PaginatedResponse<UserResponse> getAllUsers(Pageable pageable, String search);
    UserResponse registerUser(UserRegistration registration);
}
