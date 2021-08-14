package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.request.ConfirmUserRegistration;
import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.repository.UserRegistrationTokenRepository;
import com.aline.core.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j(topic = "Users Integration Test")
@DisplayName("Users Integration Test")
@Sql(scripts = "classpath:scripts/members.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class UsersIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRegistrationTokenRepository tokenRepository;

    @Test
    void test_registerUser_status_isCreated_and_location_is_in_header_when_register_memberUser() throws Exception {
        // Create user first.
        createDefaultMemberUser("member", "P@ssword321");
    }

    @Test
    void test_registerUser_status_isCreated_and_location_is_in_header_when_register_adminUser() throws Exception {
        AdminUserRegistration adminUserRegistration =
                AdminUserRegistration.builder()
                        .email("test@test.com")
                        .firstName("Admin")
                        .lastName("Boy")
                        .username("adminboy")
                        .password("P@ssword123")
                        .build();
        String adminBody = mapper.writeValueAsString(adminUserRegistration);
        mockMvc.perform(post("/users/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andExpect(jsonPath("$.username").value("adminboy"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("Boy"));
    }

    @Test
    void test_registerUser_status_is_notFound_when_membershipId_doesNotExist() throws Exception {

        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                        .username("member")
                        .password("P@ssword123")
                        .membershipId("87654321")
                        .lastFourOfSSN("2222")
                        .build();
        String memberBody = mapper.writeValueAsString(memberUserRegistration);
        mockMvc.perform(post("/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_registerUser_status_is_notFound_when_ssn_doesNotMatch() throws Exception {

        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                        .username("member")
                        .password("P@ssword123")
                        .membershipId("12345678")
                        .lastFourOfSSN("5555")
                        .build();
        String memberBody = mapper.writeValueAsString(memberUserRegistration);
        mockMvc.perform(post("/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_registerUser_status_is_conflict_when_memberUserAlreadyExists_for_membershipId() throws Exception {
        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                        .username("member")
                        .password("P@ssword123")
                        .membershipId("12345678")
                        .lastFourOfSSN("2222")
                        .build();
        String memberBody = mapper.writeValueAsString(memberUserRegistration);
        mockMvc.perform(post("/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("member"));

        MemberUserRegistration alreadyExistsRegistration =
                MemberUserRegistration.builder()
                        .username("alreadyexists")
                        .password("P@ssword123")
                        .membershipId("12345678")
                        .lastFourOfSSN("2222")
                        .build();
        String memberBody2 = mapper.writeValueAsString(alreadyExistsRegistration);
        mockMvc.perform(post("/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody2))
                .andExpect(status().isConflict());
    }

    @Test
    void test_registerUser_creates_a_userRegistrationToken() throws Exception {
        // Create user first.
        User user = createDefaultMemberUser("testboy", "P@ssword123");

        assertNotNull(user);

        UserRegistrationToken token = tokenRepository.findByUserId(user.getId()).orElse(null);

        assertNotNull(token);
    }

    @Test
    void test_confirmUserRegistration_enables_user_when_token_is_valid_and_user_exists() throws Exception {
        // Create user first.
        User user = createDefaultMemberUser("testboy", "P@ssword123");

        assertNotNull(user);

        UserRegistrationToken token = tokenRepository.findByUserId(user.getId()).orElse(null);

        assertNotNull(token);

        String tokenString = token.getToken().toString();

        ConfirmUserRegistration confirmUserRegistration = ConfirmUserRegistration.builder()
                .token(tokenString)
                .build();

        String body = mapper.writeValueAsString(confirmUserRegistration);

        mockMvc.perform(post("/users/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.enabled").value(true))
                .andDo(print());
    }

    /**
     * Create a default user with the first member in
     * the members.sql
     * @return The user grabbed from the MockMVC test results.
     * @throws Exception No exception handling in tests.
     */
    private User createDefaultMemberUser(String username, String password) throws Exception {
        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                        .username(username)
                        .password(password)
                        .membershipId("12345678")
                        .lastFourOfSSN("2222")
                        .build();
        String memberBody = mapper.writeValueAsString(memberUserRegistration);
        MvcResult result = mockMvc.perform(post("/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        UserResponse userResponse = mapper.readValue(response.getContentAsString(), UserResponse.class);

        log.info("User ID: {}", userResponse.getId());

        return userRepository.findById(userResponse.getId()).orElse(null);
    }

}
