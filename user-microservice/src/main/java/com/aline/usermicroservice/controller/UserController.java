package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.ConfirmUserRegistration;
import com.aline.core.dto.request.OtpAuthentication;
import com.aline.core.dto.request.ResetPasswordAuthentication;
import com.aline.core.dto.request.ResetPasswordRequest;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.ConfirmUserRegistrationResponse;
import com.aline.core.dto.response.ContactMethod;
import com.aline.core.dto.response.PaginatedResponse;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.model.user.UserRole;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PreAuthorize("hasAuthority('member')")
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

    @Operation(description = "Get a paginated response of users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated response was sent. It may have an empty content array which means there are no users.")
    })
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable, @RequestParam(defaultValue = "") String search) {
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
    @PostMapping("/otp-authentication")
    public ResponseEntity<Void> authenticateOtp(@Valid @RequestBody OtpAuthentication authentication) {
        passwordService.verifyOtp(authentication.getOtp(), authentication.getUsername());
        return ResponseEntity.ok().build();
    }

}
