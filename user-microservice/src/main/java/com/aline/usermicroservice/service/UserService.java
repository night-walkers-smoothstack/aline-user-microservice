package com.aline.usermicroservice.service;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.UnauthorizedException;
import com.aline.core.exception.UnprocessableException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.Applicant;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.model.user.UserRole;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.SearchSpecification;
import com.aline.core.util.SimpleSearchSpecification;
import com.aline.usermicroservice.service.function.UserRegistrationConsumer;
import com.aline.usermicroservice.service.registration.UserRegistrationHandler;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UserServiceImpl is an implementation of the UserService
 * interface. This class contains logic to help differentiate
 * the different implementations of the UserRegistration abstract class
 * and map them to their correct UserRegistrationHandler. It also
 * contains basic CRUD methods for use in the controller.
 * <br/> <br/>
 * This class also suppresses "unchecked" and "rawtypes" warnings.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
public class UserService {

    private final UserRepository repository;
    private final ModelMapper modelMapper;

    // Retrieve a list of UserRegistrationHandler implementations
    private final List<UserRegistrationHandler> handlers;
    private Map<Class<? extends UserRegistration>, UserRegistrationHandler> handlerMap;

    /**
     * Initialize the class after injection.
     * This method converts the list of UserRegistrationHandler implementations
     * into a map with the UserRegistration implementation as the key.
     *
     * This will be used when registering the user.
     */
    @PostConstruct
    public void init() {
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(UserRegistrationHandler::registersAs, Function.identity()));
    }

    /**
     * Get a user by ID.
     * @param id The ID to query.
     * @return A UserResponse of the queried user.
     */
    @PreAuthorize("permitAll()")
    @PostAuthorize("@authService.canAccess(returnObject)")
    public UserResponse getUserById(Long id) {
        return mapToDto(repository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    /**
     * Returns a paginated response of all users.
     * @param pageable Pageable passed in from controller.
     * @param search The search term.
     * @return A paginated response of UserResponse DTOs.
     */
    @PreAuthorize("hasAnyAuthority(@roles.admin, @roles.employee)")
    public PaginatedResponse<UserResponse> getAllUsers(Pageable pageable, String search) {
        SimpleSearchSpecification<User> spec = new SimpleSearchSpecification<>(search);
        Page<User> usersPage = repository.findAll(spec, pageable);
        Page<UserResponse> userResponsePage = usersPage.map(this::mapToDto);
        return new PaginatedResponse<>(userResponsePage.getContent(), pageable, userResponsePage.getTotalElements());
    }

    /**
     * Helper method to map a user to a user response.
     * <br/>
     * If the user has a role of MEMBER then it will
     * reach into the MemberUser's linked applicant
     * to retrieve the properties <code>firstName</code>,
     * <code>lastName</code>, and <code>email</code>.
     * @param user User to map.
     * @return A UserResponse mapped from a User entity.
     */
    public UserResponse mapToDto(User user) {
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setRole(user.getUserRole());

        if (user.getUserRole() == UserRole.MEMBER) {
            MemberUser memberUser = (MemberUser) user;

            Applicant applicant = memberUser.getMember().getApplicant();

            String firstName = applicant.getFirstName();
            String lastName = applicant.getLastName();
            String email = applicant.getEmail();

            userResponse.setFirstName(firstName);
            userResponse.setLastName(lastName);
            userResponse.setEmail(email);
        }

        return userResponse;
    }

    /**
     * Handles the registration of user. This class
     * utilizes generic type list injection that is available
     * in Spring to have a collection of UserRegistrationHandlers
     * on hand. That collection is then converted in a map that
     * allows access to a single UserRegistrationHandler by the
     * {@link UserRegistrationHandler#registersAs()} method.
     *
     * <br/>
     *
     * <p>
     *     For example: <br/>
     *     <code>
     *         handlerMap = new HashMap<>(); <br/>
     *         handlerMap.put(handler.registersAs(), handler);
     *          <br/> <br/>
     *         // We can now access a handler by calling the map method below: <br/>
     *         handlerMap.get(MemberUser.class); // Returns a MemberUserRegistrationHandler
     *     </code>
     * </p>
     * @param registration The UserRegistration DTO passed from the controller.
     * @return A UserResponse returned from the handler.
     */
    public UserResponse registerUser(@Valid UserRegistration registration, @Nullable UserRegistrationConsumer consumer) {
        val handler = handlerMap.get(registration.getClass());
        User registered = handler.register(registration);
        if (consumer != null)
            consumer.onRegistrationComplete(registered);
        return handler.mapToResponse(registered);
    }

    public void enableUser(Long id) {
        User user = repository.findById(id).orElseThrow(UserNotFoundException::new);
        if (user.isEnabled())
            throw new UnprocessableException("Cannot enable a user that is already enabled.");
        user.setEnabled(true);
        repository.save(user);
    }

    /**
     * Find a user by a registration token.
     * @param token The token associated with the user.
     * @return The user that was associated with the passed token.
     */
    public User getUserByToken(UserRegistrationToken token) {
        return repository.findByToken(token).orElseThrow(UserNotFoundException::new);
    }

    /**
     * Get current user information.
     * @return The current authenticated user.
     */
    public User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return repository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Not authorized to access this user."));
    }
}
