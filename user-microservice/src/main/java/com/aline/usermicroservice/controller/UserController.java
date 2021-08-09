package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.UserProfileUpdate;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import io.swagger.annotations.Api;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Api("/users")
@RestController
public class UserController implements IUserController {

    @Override
    public ResponseEntity<UserResponse> getById(Long aLong) {
        return null;
    }

    @Override
    public ResponseEntity<PaginatedResponse<UserResponse>> getAll(Pageable pageable, String search) {
        return null;
    }

    @Override
    public ResponseEntity<UserResponse> post(UserRegistration postBody) {
        return null;
    }

    @Override
    public ResponseEntity<Void> update(Long aLong, UserProfileUpdate updateBody) {
        return null;
    }

    @Override
    public ResponseEntity<Void> delete(Long aLong) {
        return null;
    }
}
