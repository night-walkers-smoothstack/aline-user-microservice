package com.aline.usermicroservice.service;

import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.notfound.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j(topic = "Authorization Service")
@RequiredArgsConstructor
public class AuthorizationService {

    public boolean hasAccess(ResponseEntity<UserResponse> response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();
        UserResponse userResponse = Optional.ofNullable(response.getBody())
                .orElseThrow(UserNotFoundException::new);
        return username.equals(userResponse.getUsername());
    }

}
