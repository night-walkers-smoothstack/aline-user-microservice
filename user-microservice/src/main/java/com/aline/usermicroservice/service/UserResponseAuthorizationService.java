package com.aline.usermicroservice.service;

import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.UserRole;
import com.aline.core.security.service.AbstractAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class UserResponseAuthorizationService extends AbstractAuthorizationService<UserResponse> {

    @Override
    protected boolean canAccess(UserResponse responseBody) {
        return (responseBody.getUsername().equals(getUsername()) ||
                getRole() == UserRole.EMPLOYEE ||
                getRole() == UserRole.ADMINISTRATOR);
    }

}
