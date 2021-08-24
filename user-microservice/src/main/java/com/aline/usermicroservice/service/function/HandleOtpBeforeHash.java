package com.aline.usermicroservice.service.function;

import com.aline.core.model.user.User;

@FunctionalInterface
public interface HandleOtpBeforeHash {
    void handle(String otp, User user);
}
