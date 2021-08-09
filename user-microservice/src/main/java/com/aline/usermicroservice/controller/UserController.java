package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.UserProfileUpdate;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Api("/users")
@RestController
public class UserController implements IUserController {
    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<PaginatedResponse<UserResponse>> getAllUsers() {
        return null;
    }

    @Override
    public ResponseEntity<UserResponse> registerUser(UserRegistration registration) {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateUser(Long id, UserProfileUpdate profileUpdate) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        return null;
    }
}
