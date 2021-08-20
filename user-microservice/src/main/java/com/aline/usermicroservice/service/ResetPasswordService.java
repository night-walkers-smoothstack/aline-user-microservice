package com.aline.usermicroservice.service;

import com.aline.core.dto.request.ResetPasswordAuthentication;
import com.aline.core.dto.request.ResetPasswordRequest;
import com.aline.core.exception.UnauthorizedException;
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
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final PasswordEncoder passwordEncoder;
    private final OneTimePasscodeRepository repository;
    private final UserRepository userRepository;
    private final RandomNumberGenerator rng;

    @Transactional(rollbackOn = {
            UserNotFoundException.class,
            UnprocessableException.class
    })
    public OneTimePasscode createResetPasswordRequest(ResetPasswordAuthentication authentication, @Nullable HandleOtpBeforeHash handleOtpBeforeHash) {
        User user = userRepository.findByUsername(authentication.getUsername())
                .orElseThrow(UserNotFoundException::new);
        if (user.getUserRole() == UserRole.MEMBER) {
            MemberUser memberUser = (MemberUser) user;
            if (memberUser.getMember().getApplicant().getEmail().equals(authentication.getEmail())) {
                throw new UserNotFoundException();
            }
        } else if (user.getUserRole() == UserRole.ADMINISTRATOR) {
            AdminUser adminUser = (AdminUser) user;
            if (adminUser.getEmail().equals(authentication.getEmail())) {
                throw new UserNotFoundException();
            }
        }
        String otpStr = rng.generateRandomNumberString(6);
        String hashedOtp = passwordEncoder.encode(otpStr);
        OneTimePasscode otp = OneTimePasscode.builder()
                .otp(hashedOtp)
                .user(user)
                .build();
        if (handleOtpBeforeHash != null) {
            handleOtpBeforeHash.handle(otpStr, user);
        }
        return repository.save(otp);
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

        if (!passwordEncoder.matches(otp.getOtp(), request.getOtp()))
            throw new UnauthorizedException("One-time password is incorrect.");

        String hashedNewOtp = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedNewOtp);

        userRepository.save(user);
    }

}
