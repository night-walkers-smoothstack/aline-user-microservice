package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public interface UserRegistrationHandler<Registration extends UserRegistration> {
    Class<Registration> registersAs();
    UserResponse register(Registration registration);
}
