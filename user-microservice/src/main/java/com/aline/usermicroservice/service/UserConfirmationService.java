package com.aline.usermicroservice.service;

import com.aline.core.dto.response.ConfirmUserRegistrationResponse;
import com.aline.core.exception.NotCreatedException;
import com.aline.core.exception.gone.TokenExpiredException;
import com.aline.core.exception.notfound.TokenNotFoundException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.repository.UserRegistrationTokenRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

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
    public ConfirmUserRegistrationResponse confirmRegistration(@NonNull UserRegistrationToken token) {
        if (token.isExpired()) {
            repository.delete(token);
            throw new TokenExpiredException();
        }
        User user = userService.getUserByToken(token);
        userService.enableUser(user.getId());
        repository.delete(token);

        return ConfirmUserRegistrationResponse.builder()
                .username(user.getUsername())
                .confirmedAt(LocalDateTime.now())
                .enabled(user.isEnabled())
                .build();
    }

    /**
     * Get token by ID
     * @param id The string id that will be converted into a UUID.
     * @return The token that is found with that ID.
     * @throws TokenNotFoundException If the token does not exist.
     */
    public UserRegistrationToken getTokenById(String id) {
        UUID uuid = UUID.fromString(id);
        return repository.findById(uuid).orElseThrow(TokenNotFoundException::new);
    }

}
