package com.aline.usermicroservice.authorization;

import com.aline.core.dto.response.UserResponse;
import com.aline.core.security.service.AbstractAuthorizationService;
import org.springframework.stereotype.Component;

@Component("authService")
public class UserAuthorization extends AbstractAuthorizationService<UserResponse> {
    @Override
    public boolean canAccess(UserResponse returnObject) {
        return (returnObject.getUsername().equals(getUsername()) || roleIsManagement());
    }

    public boolean canAccess(long userId) {
        return (getUser().getId() == userId) || roleIsManagement();
    }
}
