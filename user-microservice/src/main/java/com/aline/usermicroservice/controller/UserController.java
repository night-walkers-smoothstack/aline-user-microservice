package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.usermicroservice.service.UserService;
import com.aline.usermicroservice.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/registration")
    public UserResponse registerUser(@RequestBody UserRegistration registration) {
        return userService.registerUser(registration);
    }

}
