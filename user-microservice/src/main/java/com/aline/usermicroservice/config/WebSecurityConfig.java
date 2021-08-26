package com.aline.usermicroservice.config;

import com.aline.core.annotations.WebSecurityConfiguration;
import com.aline.core.security.config.AbstractWebSecurityConfig;

@WebSecurityConfiguration
public class WebSecurityConfig extends AbstractWebSecurityConfig {
    @Override
    protected String[] publicAntMatchers() {
        return new String[] {
                "/users/**"
        };
    }
}
