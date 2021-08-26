package com.aline.usermicroservice.config;

import com.aline.core.annotations.WebSecurityConfiguration;
import com.aline.core.security.config.AbstractWebSecurityConfig;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
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
}
