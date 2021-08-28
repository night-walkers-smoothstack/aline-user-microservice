package com.aline.usermicroservice.service;

import com.aline.core.aws.email.EmailService;
import com.aline.core.aws.sms.SMSService;
import com.aline.core.aws.sms.SMSType;
import com.aline.core.config.AppConfig;
import com.aline.core.dto.request.ResetPasswordAuthentication;
import com.aline.core.dto.request.ResetPasswordRequest;
import com.aline.core.exception.UnprocessableException;
import com.aline.core.exception.notfound.TokenNotFoundException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.exception.unauthorized.IncorrectOTPException;
import com.aline.core.model.OneTimePasscode;
import com.aline.core.model.user.AdminUser;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.User;
import com.aline.core.repository.OneTimePasscodeRepository;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.RandomNumberGenerator;
import com.aline.usermicroservice.service.function.HandleOtpBeforeHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Reset Password Service")
public class ResetPasswordService {

    private final PasswordEncoder passwordEncoder;
    private final OneTimePasscodeRepository repository;
    private final UserRepository userRepository;
    private final RandomNumberGenerator rng;
    private final SMSService smsService;
    private final EmailService emailService;
    private final AppConfig appConfig;

    @Transactional(rollbackOn = {
            UserNotFoundException.class,
            UnprocessableException.class
    })
    public void createResetPasswordRequest(ResetPasswordAuthentication authentication, @Nullable HandleOtpBeforeHash handleOtpBeforeHash) {
        log.info("Finding user with username {}", authentication.getUsername());
        User user = userRepository.findByUsername(authentication.getUsername())
                .orElseThrow(UserNotFoundException::new);
        // If user already has an OTP.
        // Delete the current one and create another.
        log.info("Delete OTP if user already has one.");
        repository.deleteByUserId(user.getId());

        String otpStr = rng.generateRandomNumberString(6);

        if (handleOtpBeforeHash != null) {
            log.info("Handling OTP before it is hashed...");
            handleOtpBeforeHash.handle(otpStr, user);
        }

        createOneTimePasscode(otpStr, user);
    }

    /**
     * Create a OneTimePasscode entity.
     * @param otpStr The One-Time Passcode string
     * @param user The user to attach it to.
     */
    public void createOneTimePasscode(String otpStr, User user) {
        log.info("Hashing OTP for password reset...");
        String hashedOtp = passwordEncoder.encode(otpStr);
        OneTimePasscode otp = OneTimePasscode.builder()
                .otp(hashedOtp)
                .user(user)
                .build();
        log.info("Saving OTP...");
        repository.save(otp);
    }

    /**
     * Check if OTP exists
     */
    public void verifyOtp(String otp, String username) {
        OneTimePasscode otpEntity = repository.findByUserUsername(username)
                        .orElseThrow(IncorrectOTPException::new);
        if(!passwordEncoder.matches(otp, otpEntity.getOtp()))
            throw new IncorrectOTPException();
        otpEntity.setChecked(true);
        repository.save(otpEntity);
    }

    /**
     * Send OTP message to a user.
     * @param otp The OTP generated.
     * @param user The user being sent the OTP.
     */
    public void sendOTPMessage(String otp, User user) {

        String phoneNumber = null;

        switch (user.getUserRole()) {
            case MEMBER:
                phoneNumber = ((MemberUser) user).getMember()
                        .getApplicant().getPhone();
                break;
            case ADMINISTRATOR:
            case EMPLOYEE:
                phoneNumber = ((AdminUser) user).getPhone();
        }

        if (phoneNumber == null) {
            log.info("No phone number was found to send this SMS message to.");
            throw new UnprocessableException("No phone number was found to send this SMS message to.");
        }

        String message = String.format("Here is your password reset one-time passcode: %s", otp);
        smsService.sendSMSMessage(phoneNumber, message, SMSType.TRANSACTIONAL);
    }

    /**
     * Send OTP email to a user.
     * @param otp The OTP generated.
     * @param user The user being sent the OTP.
     */
    public void sendOTPEmail(String otp, User user) {

        String email = null;

        switch (user.getUserRole()) {
            case MEMBER:
                email = ((MemberUser) user).getMember()
                        .getApplicant().getEmail();
                break;
            case ADMINISTRATOR:
            case EMPLOYEE:
                email = ((AdminUser) user).getEmail();
                break;
        }

        if (email == null) {
            log.info("No email was found to send this message to.");
            throw new UnprocessableException("No email was found to send this message to.");
        }

        final String template = "user/password-reset";
        final String landingPortalUrl = appConfig.getLandingPortal();
        final Map<String, String> variables = new HashMap<>();
        variables.put("landingPortalUrl", landingPortalUrl);
        variables.put("otp", otp);

        emailService.sendHtmlEmail("Password Reset", template, email, variables);
    }

    /**
     * Reset password
     * @param request the request to reset a user's password
     */
    @Transactional(rollbackOn = {
            UserNotFoundException.class,
            TokenNotFoundException.class,
            DataIntegrityViolationException.class
    })
    public void resetPassword(@Valid ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(UserNotFoundException::new);
        OneTimePasscode otp = repository.findByUser(user)
                .orElseThrow(TokenNotFoundException::new);
        if (!otp.isChecked())
            throw new UnprocessableException("The One-time password has not been verified.");
        if (!user.getUsername().equals(request.getUsername()))
            throw new UnprocessableException("Cannot use this OTP for this action.");
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
            throw new UnprocessableException("New password cannot be the same as your old password.");

        if (!passwordEncoder.matches(request.getOtp(), otp.getOtp()))
            throw new IncorrectOTPException();

        String hashedNewOtp = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedNewOtp);

        repository.deleteById(otp.getId());
        userRepository.save(user);
    }

}
