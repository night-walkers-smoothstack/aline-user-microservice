package com.aline.usermicroservice.config;

import com.aline.core.annotations.WebSecurityConfiguration;
import com.aline.core.security.config.AbstractWebSecurityConfig;

/**
 * Web Security configuration
 */
@WebSecurityConfiguration
public class WebSecurityConfig extends AbstractWebSecurityConfig {

    @Override
    public String[] publicAntMatchers() {
        return new String[]{
                "/users/**"
        };
    }
}
