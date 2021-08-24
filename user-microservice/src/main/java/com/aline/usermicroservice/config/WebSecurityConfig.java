package com.aline.usermicroservice.config;

import com.aline.core.config.AppConfig;
import com.aline.core.security.AuthenticationFilter;
import com.aline.core.security.JwtTokenVerifier;
import com.aline.core.security.service.SecurityUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;
    private final SecurityUserService service;
    private final AppConfig appConfig;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenVerifier verifier;
    private final SecretKey secretKey;
    private final ObjectMapper objectMapper;

    @Override
    @Autowired
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("SELECT username, password, enabled FROM user WHERE username = ?")
                .authoritiesByUsernameQuery("SELECT username, role, enabled FROM user WHERE username = ?")
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new AuthenticationFilter(authenticationManager(),
                        appConfig, secretKey, objectMapper))
                .addFilterAfter(verifier, AuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/v3/api-docs/**",
                        "/health",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/users/**",
                        "/webjars/**",
                        "**/swagger-resources/**").permitAll()
                .anyRequest()
                .authenticated();
    }

//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList(
                    appConfig.getAdminPortal(),
                    appConfig.getLandingPortal(),
                    appConfig.getMemberDashboard()));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(
                Arrays.asList(HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.CONTENT_LENGTH,
                        HttpHeaders.ACCEPT,
                        HttpHeaders.ORIGIN,
                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        configuration.setExposedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));

        val source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
