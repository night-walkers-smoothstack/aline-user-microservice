package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.User;
import com.aline.usermicroservice.service.UserService;
import com.aline.usermicroservice.util.SimpleCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController extends SimpleCrudController<UserResponse, User, UserRegistration, UserRegistration, Long, UserService> {

    public UserController(UserService service) {
        super(service);
    }

    @Override
    protected Long getId(UserResponse userResponse) {
        return userResponse.getId();
    }
}
