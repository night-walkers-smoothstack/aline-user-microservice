package com.aline.usermicroservice.config;

import com.aline.core.annotation.WebSecurityConfiguration;
import com.aline.core.dto.response.UserProfile;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.security.config.AbstractWebSecurityConfig;
import com.aline.core.security.service.AbstractAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@WebSecurityConfiguration
@Slf4j(topic = "Web Security Configuration")
public class WebSecurityConfig extends AbstractWebSecurityConfig {

    @Override
    protected void configureHttp(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST,
                        "/users/registration",
                        "/users/confirmation",
                        "/users/otp-authentication",
                        "/users/password-reset-otp")
                .permitAll()
                .antMatchers(HttpMethod.PUT, "/users/password-reset")
                .permitAll();
    }

}
