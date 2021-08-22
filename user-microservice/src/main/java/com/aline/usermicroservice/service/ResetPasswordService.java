package com.aline.usermicroservice.service;

import com.aline.core.aws.sms.SMSService;
import com.aline.core.aws.sms.SMSType;
import com.aline.core.dto.request.ResetPasswordAuthentication;
import com.aline.core.dto.request.ResetPasswordRequest;
import com.aline.core.exception.ForbiddenException;
import com.aline.core.exception.UnprocessableException;
import com.aline.core.exception.notfound.TokenNotFoundException;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.OneTimePasscode;
import com.aline.core.model.user.AdminUser;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRole;
import com.aline.core.repository.OneTimePasscodeRepository;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.RandomNumberGenerator;
import com.aline.usermicroservice.service.function.HandleOtpBeforeHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Reset Password Service")
public class ResetPasswordService {

    private final PasswordEncoder passwordEncoder;
    private final OneTimePasscodeRepository repository;
    private final UserRepository userRepository;
    private final RandomNumberGenerator rng;
    private final SMSService smsService;

    @Transactional(rollbackOn = {
            UserNotFoundException.class,
            UnprocessableException.class
    })
    public void createResetPasswordRequest(ResetPasswordAuthentication authentication, @Nullable HandleOtpBeforeHash handleOtpBeforeHash) {
        log.info("Finding user with username {}", authentication.getUsername());
        User user = userRepository.findByUsername(authentication.getUsername())
                .orElseThrow(UserNotFoundException::new);
        String otpStr = rng.generateRandomNumberString(6);

        log.info("Hashing OTP for password reset...");
        String hashedOtp = passwordEncoder.encode(otpStr);
        OneTimePasscode otp = OneTimePasscode.builder()
                .otp(hashedOtp)
                .user(user)
                .build();
        if (handleOtpBeforeHash != null) {
            log.info("Handling OTP before it is hashed...");
            handleOtpBeforeHash.handle(otpStr, user);
        }

        log.info("Saving OTP...");
        repository.save(otp);
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

        if (phoneNumber != null) {
            String message = String.format("Here is your password reset one-time passcode: %s", otp);
            smsService.sendSMSMessage(phoneNumber, message, SMSType.TRANSACTIONAL);
        } else {
            log.info("No phone number was found to send this SMS message to.");
            throw new UnprocessableException("No phone number was found to send this SMS message to.");
        }

    }

    @Transactional(rollbackOn = {
            UserNotFoundException.class,
            TokenNotFoundException.class
    })
    public void resetPassword(@Valid ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(UserNotFoundException::new);
        OneTimePasscode otp = repository.findByUser(user)
                .orElseThrow(TokenNotFoundException::new);
        if (!user.getUsername().equals(request.getUsername()))
            throw new UnprocessableException("Cannot use this OTP for this action.");

        if (!passwordEncoder.matches(request.getOtp(), otp.getOtp()))
            throw new ForbiddenException("One-time password is incorrect.");

        String hashedNewOtp = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedNewOtp);

        userRepository.save(user);
    }

}
