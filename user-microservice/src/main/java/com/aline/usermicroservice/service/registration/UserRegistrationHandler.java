package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.User;
import org.springframework.stereotype.Component;

@Component
public interface UserRegistrationHandler<U extends User, Registration extends UserRegistration> {
    Class<Registration> registersAs();
    U register(Registration registration);
    UserResponse mapToResponse(U u);
}
