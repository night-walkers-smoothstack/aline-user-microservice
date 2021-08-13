package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.usermicroservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Value("${server.port}")
    private int port;

    private final UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistration registration) {
        UserResponse response = userService.registerUser(registration);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/{id}")
                .port(port)
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

}
