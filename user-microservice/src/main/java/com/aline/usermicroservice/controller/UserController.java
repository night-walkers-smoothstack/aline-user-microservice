package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.ConfirmUserRegistration;
import com.aline.core.dto.request.OtpAuthentication;
import com.aline.core.dto.request.ResetPasswordAuthentication;
import com.aline.core.dto.request.ResetPasswordRequest;
import com.aline.core.dto.request.UserAvatarRequest;
import com.aline.core.dto.request.UserProfileUpdate;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.ConfirmUserRegistrationResponse;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserProfile;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.model.user.UserRole;
import com.aline.usermicroservice.service.AvatarService;
import com.aline.usermicroservice.service.ResetPasswordService;
import com.aline.usermicroservice.service.UserConfirmationService;
import com.aline.usermicroservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@Tag(name = "Users")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j(topic = "User Controller")
public class UserController {

    @Value("${server.port}")
    private int port;

    private final UserService userService;
    private final UserConfirmationService confirmationService;
    private final ResetPasswordService passwordService;
	private final AvatarService avatarService;
    @Operation(description = "Get a user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User with specified ID found."),
            @ApiResponse(responseCode = "404", description = "User does not exist.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userResponse);
    }

	@Operation(description = "Update avatar image for a user")
	@PutMapping("/current/avatar")
	public ResponseEntity<Void> putAvatar(
			@CurrentSecurityContext(expression = "authentication") Authentication authentication,
			@RequestBody UserAvatarRequest image) {
		UserResponse currentUser = userService.getCurrentUser(authentication);
		avatarService.putAvatar(currentUser.getId(), image);
		return ResponseEntity.ok().build();
	}

	@Operation(description = "Get avatar image for a user")
	@GetMapping("/current/avatar")
	public ResponseEntity<UserAvatarRequest> getAvatar(
			@CurrentSecurityContext(expression = "authentication") Authentication authentication) {
		UserResponse currentUser = userService.getCurrentUser(authentication);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
				.body(avatarService.getAvatar(currentUser.getId()));
	}
    @Operation(description = "Get a paginated response of users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated response was sent. It may have an empty content array which means there are no users.")
    })
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable,
                                                          @RequestParam(defaultValue = "") String search,
                                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        PaginatedResponse<UserResponse> userResponsePage = userService.getAllUsers(pageable, search);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userResponsePage);
    }

    @Operation(description = "Create a new user registration")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User has been successfully registered."),
            @ApiResponse(responseCode = "400", description = "The user registration DTO contained bad data."),
            @ApiResponse(responseCode = "409", description = "There was a data conflict when creating the user."),
            @ApiResponse(responseCode = "502", description = "The user registration confirmation email was not sent.")
    })
    @PostMapping("/registration")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistration registration) {
        // Create a registration token for a member user when registration is successful.
        UserResponse response = userService.registerUser(registration, user -> {
            if (UserRole.valueOf(user.getRole().toUpperCase()) == UserRole.MEMBER) {
                confirmationService.sendMemberUserConfirmationEmail((MemberUser) user);
            }
        });
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/{id}")
                .port(port)
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Confirms the registration of a user and enables their account.
     * @param confirmUserRegistration The confirm registration dto sent from the front-end
     * @return ConfirmUserRegistrationResponse ResponseEntity
     */
    @Operation(description = "Confirm user registration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registration was successfully confirmed."),
            @ApiResponse(responseCode = "404", description = "User or registration was not found."),
            @ApiResponse(responseCode = "410", description = "Token does not exist or is expired.")
    })
    @PostMapping("/confirmation")
    public ResponseEntity<ConfirmUserRegistrationResponse> confirmUserRegistration(@Valid @RequestBody ConfirmUserRegistration confirmUserRegistration) {

        UserRegistrationToken token = confirmationService.getTokenById(confirmUserRegistration.getToken());
        ConfirmUserRegistrationResponse response = confirmationService.confirmRegistration(token);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Sends a reset OTP to the requesting user
     * specified in the DTO.
     * @param resetPasswordAuthentication the DTO that contains the user information
     * @return Response Entity of Void
     */
    @Operation(description = "Create a one-time passcode to reset a user's password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password OTP is created."),
            @ApiResponse(responseCode = "404", description = "User to create the OTP for was not found."),
            @ApiResponse(responseCode = "422", description = "One-time passcode was not sent through either SMS or Email.")
    })
    @PostMapping("/password-reset-otp")
    public ResponseEntity<Void> createPasswordResetOtp(@Valid @RequestBody ResetPasswordAuthentication resetPasswordAuthentication) {
        passwordService.createResetPasswordRequest(resetPasswordAuthentication,
                (otp, user) -> {
                    log.info("Contact Method: {}", resetPasswordAuthentication.getContactMethod());
                    switch (resetPasswordAuthentication.getContactMethod()) {
                        case PHONE:
                            log.info("Send password reset message to {}. OTP is {}", user.getUsername(), otp);
                            passwordService.sendOTPMessage(otp, user);
                            break;
                        case EMAIL:
                            log.info("Send password reset email to {}. OTP is {}", user.getUsername(), otp);
                            passwordService.sendOTPEmail(otp, user);
                            break;
                    }
                });
        return ResponseEntity.ok().build();
    }

    /**
     * Reset password based on information
     * passed in through the request.
     * @param request The request DTO that contains the new password and OTP
     * @return Response Entity of Void
     */
    @Operation(description = "Reset user password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password successfully reset."),
            @ApiResponse(responseCode = "")
    })
    @PutMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Authenticates the OTP and checks to make sure
     * it exists with the correct username.
     * @param authentication The DTO that contains the username and the OTP.
     * @return Ok response entity.
     */
    @Operation(description = "Verify the OTP and allow it to be used.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One-time passcode was successfully verified."),
            @ApiResponse(responseCode = "401", description = "One-time passcode was not correct and was not verified.")
    })
    @PostMapping("/otp-authentication")
    public ResponseEntity<Void> authenticateOtp(@Valid @RequestBody OtpAuthentication authentication) {
        passwordService.verifyOtp(authentication.getOtp(), authentication.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * Get the current authenticated user
     * @param authentication The security context authentication object
     * @return A user response of the current authenticated user
     */
    @Operation(description = "Get the current authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieved current authenticated user."),
            @ApiResponse(responseCode = "401", description = "Not authorized to access the user.")
    })
    @GetMapping("/current")
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentSecurityContext(expression = "authentication")
                                                       Authentication authentication) {
        UserResponse currentUser = userService.getCurrentUser(authentication);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(currentUser);
    }

    @Operation(description = "Get a user profile by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of user profile"),
            @ApiResponse(responseCode = "404", description = "User profile does not exist. (May not be a member)"),
            @ApiResponse(responseCode = "401", description = "Not authorized to access the user profile")
    })
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable long id) {
        UserProfile profile = userService.getUserProfileById(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(profile);
    }

    @Operation(description = "Get a user profile of current logged-in user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of user profile"),
            @ApiResponse(responseCode = "404", description = "User profile does not exist. (May not be a member)"),
            @ApiResponse(responseCode = "401", description = "Not authorized to access the user profile")
    })
    @GetMapping("/current/profile")
    public ResponseEntity<UserProfile> getCurrentUserProfile(@CurrentSecurityContext(expression = "authentication")
                                                             Authentication authentication) {
        UserProfile currentProfile = userService.getCurrentUserProfile(authentication);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(currentProfile);
    }

    @Operation(description = "Update user profile by user ID")
    @PutMapping("/{id}/profile")
    public ResponseEntity<Void> updateUserProfileById(@PathVariable long id, @RequestBody UserProfileUpdate update) {
        userService.updateUserProfile(id, update);
        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(description = "Update current logged-in user profile")
    @PutMapping("/current/profile")
    public ResponseEntity<Void> updateUserProfileById(@CurrentSecurityContext(expression = "authentication")
                                                      Authentication authentication, @RequestBody UserProfileUpdate update) {
        userService.updateCurrentUserProfile(authentication, update);
        return ResponseEntity
                .noContent()
                .build();
    }

	@Operation(description = "Disable/Enabled current logged-in user profile")
	@PutMapping("/current/profile/status")
	public ResponseEntity<Void> disableUserProfileById(
			@CurrentSecurityContext(expression = "authentication") Authentication authentication,
			@RequestBody Boolean status) {
		userService.disableCurrentUserProfile(authentication, status);
		return ResponseEntity.noContent().build();
	}

}
