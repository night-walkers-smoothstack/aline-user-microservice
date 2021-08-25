package com.aline.usermicroservice.config;

import com.aline.core.security.config.AbstractWebSecurityConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Web Security configuration
 */
@Configuration
public class WebSecurityConfig extends AbstractWebSecurityConfig {

    @Override
    public String[] publicAntMatchers() {
        return new String[]{
                "/users/**"
        };
    }
}
