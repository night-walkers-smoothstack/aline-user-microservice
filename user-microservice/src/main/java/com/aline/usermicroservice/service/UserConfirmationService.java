package com.aline.usermicroservice.service;

import com.aline.core.aws.email.EmailService;
import com.aline.core.config.AppConfig;
import com.aline.core.dto.response.ConfirmUserRegistrationResponse;
import com.aline.core.exception.BadRequestException;
import com.aline.core.exception.UnprocessableException;
import com.aline.core.exception.gone.TokenExpiredException;
import com.aline.core.exception.notfound.TokenNotFoundException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.repository.UserRegistrationTokenRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "User Confirmation Service")
public class UserConfirmationService {

    private final AppConfig appConfig;
    private final UserService userService;
    private final EmailService emailService;
    private final UserRegistrationTokenRepository repository;

    /**
     * Create a user registration token for a user.
     * @param user The user to create a token for.
     * @return The token created for the user.
     */
    public UserRegistrationToken createRegistrationToken(@NonNull User user) {
        if (user.isEnabled())
            throw new UnprocessableException("Cannot create a registration confirmation token for a user that is already enabled.");
        UserRegistrationToken token = new UserRegistrationToken();
        token.setUser(user);
        return repository.save(token);
    }

    /**
     * Confirm registration and delete the token.
     * @param token The token to access the user.
     */
    @Transactional(rollbackOn = {
            UserNotFoundException.class,
            UnprocessableException.class
    })
    public ConfirmUserRegistrationResponse confirmRegistration(@NonNull UserRegistrationToken token) {
        log.info("Confirming registration token: {}", token);
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
        try  {
            UUID uuid = UUID.fromString(id);
            return repository.findById(uuid).orElseThrow(TokenNotFoundException::new);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Token format is invalid.");
        }
    }

    /**
     * Get token by user
     * @param user The user the token is being queried for
     * @return The token that is found linked to the user.
     * @throws TokenNotFoundException If the token does not exist.
     */
    public UserRegistrationToken getTokenByUser(@NonNull User user) {
        return repository.findByUserId(user.getId()).orElseThrow(TokenNotFoundException::new);
    }

    /**
     * Send the specified user a confirmation email.
     * @param user The user to create and send a confirmation email to.
     */
    public void sendMemberUserConfirmationEmail(MemberUser user) {

        final String username = user.getUsername();
        final String subject = String.format("We Need Your Confirmation, %s", username);
        final String template = "user/confirm-registration";
        final String email = user.getMember().getApplicant().getEmail();
        final String memberDashboardUrl = appConfig.getMemberDashboard();
        final String landingPortalUrl = appConfig.getLandingPortal();
        final String token = createRegistrationToken(user).getToken().toString();
        final String confirmationLink = String.format("%s/confirmation?token=%s", memberDashboardUrl, token);

        final Map<String, String> variables = new HashMap<>();
        variables.put("landingPortalUrl", landingPortalUrl);
        variables.put("confirmationLink", confirmationLink);

        emailService.sendHtmlEmail(subject, template, email, variables);
    }

}

