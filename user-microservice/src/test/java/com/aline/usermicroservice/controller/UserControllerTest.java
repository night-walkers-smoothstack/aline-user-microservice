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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User Controller Integration Test")
@Sql(scripts = "classpath:scripts/members.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void test_registerUser_status_isCreated_and_location_is_in_header_when_register_memberUser() throws Exception {
        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                        .username("member")
                        .password("P@ssword123")
                        .membershipId("12345678")
                        .build();
        String memberBody = mapper.writeValueAsString(memberUserRegistration);
        mockMvc.perform(post("/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("member"))
                .andDo(print());
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
                .andExpect(jsonPath("$.lastName").value("Boy"))
                .andDo(print());
    }

}
