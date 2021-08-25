package com.aline.usermicroservice.config;

import com.aline.core.config.AppConfig;
import com.aline.core.security.AuthenticationFilter;
import com.aline.core.security.JwtTokenVerifier;
import com.aline.core.security.config.AbstractWebSecurityConfig;
import com.aline.core.security.service.SecurityUserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

/**
 * Web Security configuration
 */
@Configuration
public class WebSecurityConfig extends AbstractWebSecurityConfig {

    public WebSecurityConfig(DataSource dataSource, PasswordEncoder passwordEncoder, SecurityUserService service, AppConfig appConfig, AuthenticationFilter authenticationFilter, JwtTokenVerifier verifier) {
        super(dataSource, passwordEncoder, service, appConfig, authenticationFilter, verifier);
    }

    @Override
    public String[] publicAntMatchers() {
        return new String[]{
                "/users/**"
        };
    }
}
