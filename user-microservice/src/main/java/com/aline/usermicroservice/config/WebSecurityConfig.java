package com.aline.usermicroservice.config;

import com.aline.core.annotations.WebSecurityConfiguration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRole;
import com.aline.core.security.config.AbstractWebSecurityConfig;
import com.aline.core.security.service.AbstractAuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@WebSecurityConfiguration
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
                .antMatchers(HttpMethod.PUT, "/users/password-reset");
    }

    @Bean
    public AbstractAuthorizationService<UserResponse> authService() {
        return new AbstractAuthorizationService<UserResponse>() {
            @Override
            public boolean canAccess(UserResponse responseBody) {
                return (responseBody.getUsername().equals(getUsername()) ||
                        getRole() == UserRole.ADMINISTRATOR ||
                        getRole() == UserRole.EMPLOYEE);
            }
        };
    }

}
