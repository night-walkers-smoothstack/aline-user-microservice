package com.aline.usermicroservice.service;

import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.user.User;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.SearchSpecification;
import com.aline.usermicroservice.service.registration.UserRegistrationHandler;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class UserServiceImpl implements UserService {

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
    @Override
    public UserResponse getUserById(Long id) {
        return mapToDto(repository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    /**
     * Returns a paginated response of all users.
     * @param pageable Pageable passed in from controller.
     * @param search The search term.
     * @return A paginated response of UserResponse DTOs.
     */
    @Override
    public PaginatedResponse<UserResponse> getAllUsers(Pageable pageable, String search) {
        SearchSpecification<User> spec = new SearchSpecification<>(search);
        Page<User> usersPage = repository.findAll(spec, pageable);
        Page<UserResponse> userResponsePage = usersPage.map(this::mapToDto);
        return new PaginatedResponse<>(userResponsePage.getContent(), pageable, userResponsePage.getTotalElements());
    }

    /**
     * Helper method to map a user to a user response.
     * @param user User to map.
     * @return A UserResponse mapped from a User entity.
     */
    protected UserResponse mapToDto(User user) {
        return modelMapper.map(user, UserResponse.class);
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
    @Override
    public UserResponse registerUser(@Valid UserRegistration registration) {
        val handler = handlerMap.get(registration.getClass());
        return handler.mapToResponse(handler.register(registration));
    }

    @Override
    public void enableUser(Long id) {
        User user = repository.findById(id).orElseThrow(UserNotFoundException::new);
        user.setEnabled(true);
        repository.save(user);
    }
}
