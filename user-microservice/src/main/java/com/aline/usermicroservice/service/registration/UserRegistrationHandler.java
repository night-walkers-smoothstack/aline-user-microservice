package com.aline.usermicroservice.service.registration;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.User;
import org.springframework.stereotype.Component;

/**
 * The UserRegistrationHandler interfaces provides
 * abstraction for derived classes that need to implement
 * logic for registering a user and persisting the entity
 * into the database.
 * @param <U> The User entity that this handler will create.
 * @param <Registration> The Registration DTO that this handler uses to create the entity.
 */
@Component
public interface UserRegistrationHandler<U extends User, Registration extends UserRegistration> {

    /**
     * Returns the class literal of the Registration DTO.
     * @return Class literal of the Registration DTO.
     */
    Class<Registration> registersAs();

    /**
     * The register method that is to be implemented.
     * @param registration The Registration DTO.
     * @return An object that extends a {@link User} entity.
     */
    U register(Registration registration);

    /**
     * This method provides an implementation of
     * mapping the entity to a {@link UserResponse} DTO.
     * @param u The User entity to be mapped.
     * @return A UserResponse mapped from the passed User entity.
     */
    UserResponse mapToResponse(U u);
}
