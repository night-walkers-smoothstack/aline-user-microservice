package com.aline.usermicroservice.service;

import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.NotCreatedException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.repository.UserRegistrationTokenRepository;
import com.aline.core.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class UserConfirmationService {

    private final UserService userService;
    private final UserRegistrationTokenRepository repository;

    /**
     * Create a user registration token for a user.
     * @param user The user to create a token for.
     * @return The token created for the user.
     */
    public UserRegistrationToken createRegistrationToken(@NonNull User user) {
        if (user.isEnabled())
            throw new NotCreatedException("Cannot create a registration confirmation token for a user that is already enabled.");
        UserRegistrationToken token = new UserRegistrationToken();
        token.setUser(user);
        return repository.save(token);
    }

    /**
     * Confirm registration and delete the token.
     * @param token The token to access the user.
     */
    @Transactional(rollbackOn = UserNotFoundException.class)
    public void confirmRegistration(@NonNull UserRegistrationToken token) {
        userService.enableUser(token.getUser().getId());
        repository.delete(token);
    }

}
