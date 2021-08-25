package com.aline.usermicroservice.service;

import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.UserRole;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j(topic = "Authorization Service")
public class AuthorizationService {

    public boolean ownsResource(@NonNull UserResponse userResponse, @NonNull Authentication authentication) {
        log.info("Resource Username: {}", userResponse.getUsername());
        log.info("Principal Username: {}", authentication.getPrincipal());
        return userResponse.getUsername().equals(authentication.getPrincipal()) ||
                userResponse.getRole() == UserRole.ADMINISTRATOR || userResponse.getRole() == UserRole.EMPLOYEE;
    }

    public boolean hasRole(@NonNull Authentication authentication) {
        log.info("Username: {}", authentication.getPrincipal());
        log.info("Authorities: {}", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", ")));
        return true;
    }

}
