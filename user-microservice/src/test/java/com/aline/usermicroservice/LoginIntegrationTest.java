package com.aline.usermicroservice;

import com.aline.core.annotations.test.SpringBootIntegrationTest;
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
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;

import java.rmi.registry.Registry;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
        createDefaultMemberUser("member_user", "P@ssword123");
        enableUser("member_user");
    }

    @Test
    void test_login_statusIsOk_when_login_is_correct() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("member_user")
                .password("P@ssword123")
                .build();
        String body = mapper.writeValueAsString(request);
        mockMvc.perform(post("/login")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void test_login_statusIsUnauthorized_when_login_is_incorrect() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("member_user")
                .password("P@ssword124")
                .build();
        String body = mapper.writeValueAsString(request);
        mockMvc.perform(post("/login")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION));
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
