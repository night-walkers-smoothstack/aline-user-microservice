package com.aline.usermicroservice.controller;

import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.request.MemberUserRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User Controller Integration Test")
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void test_userRegistrationDTO_returns_correctType() throws Exception {
        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                    .username("member")
                    .password("password")
                    .membershipId("12345678")
                    .build();
        AdminUserRegistration adminUserRegistration =
                AdminUserRegistration.builder()
                        .email("test@test.com")
                        .firstName("Admin")
                        .lastName("Boy")
                        .username("admin")
                        .password("password")
                        .build();

        String memberBody = mapper.writeValueAsString(memberUserRegistration);
        String adminBody = mapper.writeValueAsString(adminUserRegistration);

        mockMvc.perform(post("/users/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(memberBody))
                .andDo(print());

        mockMvc.perform(post("/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminBody))
                .andDo(print());
    }

}
