package com.aline.usermicroservice.service.function;

import com.aline.core.model.OneTimePasscode;
import com.aline.core.model.user.User;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface HandleOtpBeforeHash {
    void handle(String otp, @Nullable User user);
}
