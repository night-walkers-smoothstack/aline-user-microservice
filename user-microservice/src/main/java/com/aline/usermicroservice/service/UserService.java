package com.aline.usermicroservice.service;

import com.aline.core.dto.request.AddressChangeRequest;
import com.aline.core.dto.request.UserProfileUpdate;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.AddressResponse;
import com.aline.core.dto.response.ContactInfo;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserProfile;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.NotFoundException;
import com.aline.core.exception.UnauthorizedException;
import com.aline.core.exception.UnprocessableException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.Applicant;
import com.aline.core.model.Member;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.model.user.UserRole;
import com.aline.core.repository.ApplicantRepository;
import com.aline.core.repository.MemberRepository;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.SimpleSearchSpecification;
import com.aline.usermicroservice.service.function.UserRegistrationConsumer;
import com.aline.usermicroservice.service.registration.UserRegistrationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.transaction.Transactional;
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
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
public class UserService {

    private final UserRepository repository;
    private final MemberService memberService;
    private final ApplicantService applicantService;
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

            Member member = memberUser.getMember();
            Applicant applicant = member.getApplicant();

            String firstName = applicant.getFirstName();
            String lastName = applicant.getLastName();
            String email = applicant.getEmail();

            long memberId = member.getId();
            String membershipId = member.getMembershipId();

            userResponse.setFirstName(firstName);
            userResponse.setLastName(lastName);
            userResponse.setEmail(email);
            userResponse.setMemberId(memberId);
            userResponse.setMembershipId(membershipId);
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
    public UserResponse getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Not authorized to access this user."));
        return mapToDto(user);
    }

    /**
     * Get user profile by id
     * @param id The id of the user
     * @return A UserProfile DTO of the requested user ID
     */
    @PermitAll
    @PostAuthorize("@authService.canAccess(returnObject)")
    public UserProfile getUserProfileById(long id) {
        User user = repository.findById(id).orElseThrow(UserNotFoundException::new);

        if (user.getUserRole() != UserRole.MEMBER)
            throw new NotFoundException("User does not have a profile.");

        MemberUser memberUser = (MemberUser) user;
        return mapUserToProfile(memberUser);
    }

    /**
     * Get current logged-in user profile
     * @param authentication The authentication object of the logged-in user
     * @return UserProfile DTO of the currently logged-in user
     */
    public UserProfile getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Not authorized to access this user."));
        if (user.getUserRole() != UserRole.MEMBER)
            throw new NotFoundException("User does not have a profile.");

        MemberUser memberUser = (MemberUser) user;
        return mapUserToProfile(memberUser);
    }

    /**
     * Map MemberUser object to a UserProfile
     * @param memberUser The user to map to a profile
     * @return A profile of the passed user
     */
    public UserProfile mapUserToProfile(MemberUser memberUser) {
        Member member = memberUser.getMember();
        Applicant applicant = member.getApplicant();

        return UserProfile.builder()
                .username(memberUser.getUsername())
                .firstName(applicant.getFirstName())
                .middleName(applicant.getMiddleName())
                .lastName(applicant.getLastName())
                .income(applicant.getIncome())
                .membershipId(member.getMembershipId())
                .billingAddress(AddressResponse.builder()
                        .address(applicant.getAddress())
                        .city(applicant.getCity())
                        .state(applicant.getState())
                        .zipcode(applicant.getZipcode())
                        .build())
                .mailingAddress(AddressResponse.builder()
                        .address(applicant.getMailingAddress())
                        .city(applicant.getMailingCity())
                        .state(applicant.getMailingState())
                        .zipcode(applicant.getMailingZipcode())
                        .build())
                .contactInfo(ContactInfo.builder()
                        .email(applicant.getEmail())
                        .phone(applicant.getPhone())
                        .build())
                .build();
    }

    @Transactional(rollbackOn = {NotFoundException.class, UserNotFoundException.class})
    @PreAuthorize("@authService.canAccess(#userId)")
    public void updateUserProfile(long userId, UserProfileUpdate update) {
        User user = repository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getUserRole() != UserRole.MEMBER)
            throw new NotFoundException("User does not have a profile.");

        MemberUser memberUser = (MemberUser) user;
        Member member = memberUser.getMember();
        Applicant applicant = member.getApplicant();

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true);
        modelMapper.map(update, applicant);

        if (update.getUsername() != null) {
            memberUser.setUsername(update.getUsername());
        }

        applicantService.saveApplicant(applicant);
        member.setApplicant(applicant);
        memberService.saveMember(member);
        memberUser.setMember(member);
        repository.save(memberUser);
    }

    @Transactional(rollbackOn = {
            UserNotFoundException.class,
            NotFoundException.class
    })
    public void updateCurrentUserProfile(Authentication authentication, UserProfileUpdate update) {
        User user = repository.findByUsername(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
        long id = user.getId();
        updateUserProfile(id, update);
    }

	@Transactional(rollbackOn = { UserNotFoundException.class, NotFoundException.class })
	public void disableCurrentUserProfile(Authentication authentication, Boolean status) {
		User user = repository.findByUsername(authentication.getName()).orElseThrow(UserNotFoundException::new);
		long id = user.getId();
		disableUserProfile(id, status);

	}

	@Transactional(rollbackOn = { NotFoundException.class, UserNotFoundException.class })
	@PreAuthorize("permitAll()")
	public void disableUserProfile(long userId, Boolean status) {
		User user = repository.findById(userId).orElseThrow(UserNotFoundException::new);
		if (user.getUserRole() != UserRole.MEMBER)
			throw new NotFoundException("User does not have a profile.");

		MemberUser memberUser = (MemberUser) user;
		memberUser.setEnabled(false);
		repository.save(memberUser);
	}
}
