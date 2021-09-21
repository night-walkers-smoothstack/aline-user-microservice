package com.aline.usermicroservice;

import com.aline.core.annotation.test.SpringBootIntegrationTest;
import com.aline.core.aws.email.EmailService;
import com.aline.core.dto.request.AuthenticationRequest;
import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.User;
import com.aline.core.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootIntegrationTest
@Slf4j(topic = "Login Integration Test")
@DisplayName("Login Integration Test")
@Sql(scripts = {"classpath:scripts/members.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class LoginIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @MockBean
    EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        // No HBO Max pls
        doNothing().when(emailService).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void test_login_statusIsOk_when_login_is_correct() throws Exception {
        createDefaultMemberUser("member_user", "P@ssword123");
        enableUser("member_user");
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("member_user")
                .password("P@ssword123")
                .build();
        String body = mapper.writeValueAsString(request);
        mockMvc.perform(post("/login")
                        .secure(true)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void test_login_statusIsUnauthorized_when_login_is_incorrect() throws Exception {
        createDefaultMemberUser("member_user", "P@ssword123");
        enableUser("member_user");
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("member_user")
                .password("P@ssword124")
                .build();
        String body = mapper.writeValueAsString(request);
        mockMvc.perform(post("/login")
                        .secure(true)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void test_login_statusIsUnauthorized_when_login_correct_but_user_notEnabled() throws Exception {
        createDefaultMemberUser("member_user", "P@ssword123");
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("member_user")
                .password("P@ssword123")
                .build();
        String body = mapper.writeValueAsString(request);
        mockMvc.perform(post("/login")
                        .secure(true)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @WithMockUser(username = "member_user")
    void test_getCurrentUser_statusIsOk_when_user_ownsResource() throws Exception {
        createDefaultMemberUser("member_user", "P@ssword123");
        mockMvc.perform(get("/users/current").secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("member_user"));
    }

    @Test
    @WithMockUser(username = "nosey_user")
    void test_getCurrentUser_statusIsUnauthorized_when_user_doesNotOwnResource() throws Exception {
        createDefaultMemberUser("member_user", "P@ssword123");
        mockMvc.perform(get("/users/current").secure(true))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void test_getCurrentUser_statusIsForbidden_when_user_is_anonymous() throws Exception {
        createDefaultMemberUser("member_user", "P@ssword123");
        mockMvc.perform(get("/users/current").secure(true))
                .andExpect(status().isForbidden());
    }

    // Create a default member user for log in purposes.
    public void createDefaultMemberUser(String username, String password) throws Exception {
        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                        .username(username)
                        .password(password)
                        .membershipId("12345678")
                        .lastFourOfSSN("2222")
                        .build();
        String memberBody = mapper.writeValueAsString(memberUserRegistration);
        MvcResult result = mockMvc.perform(post("/users/registration")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        UserResponse userResponse = mapper.readValue(response.getContentAsString(), UserResponse.class);
        userRepository.findById(userResponse.getId());
    }

    // Enable a user by username
    public void enableUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        assertNotNull(user);
        user.setEnabled(true);
        userRepository.save(user);
    }

}
