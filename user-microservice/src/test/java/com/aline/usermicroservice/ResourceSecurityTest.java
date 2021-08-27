package com.aline.usermicroservice;

import com.aline.core.annotations.test.SpringBootIntegrationTest;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.UserRole;
import com.aline.core.security.model.SecurityUser;
import com.aline.core.security.model.UserRoleAuthority;
import com.aline.usermicroservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootIntegrationTest
@ContextConfiguration
@Sql(scripts = "classpath:scripts/members.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@Slf4j
class ResourceSecurityTest {

    private final static long FOUND_MEMBER = 1;
    private final static long FOUND_ADMIN = 2;
    private final static long NOT_FOUND = 4;
    private final static String MEMBER_USERNAME = "member_user";
    private final static String ADMIN_USERNAME = "admin_user";
    private final static String NOSEY_USERNAME = "nosey_user";
    private final static String MEMBER = "member";
    private final static String ADMIN = "administrator";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    UserResponse foundMemberUser;
    UserResponse foundAdminUser;

    @BeforeEach
    void setUp() {

        foundMemberUser = UserResponse.builder()
                .id(FOUND_MEMBER)
                .username(MEMBER_USERNAME)
                .email("member_user@email.com")
                .role(UserRole.MEMBER)
                .firstName("Member")
                .lastName("Boy")
                .enabled(true)
                .build();
        when(userService.getUserById(FOUND_MEMBER)).thenReturn(foundMemberUser);

        foundAdminUser = UserResponse.builder()
                .id(FOUND_ADMIN)
                .username(ADMIN_USERNAME)
                .email("admin@email.com")
                .role(UserRole.ADMINISTRATOR)
                .firstName("Admin")
                .lastName("Boy")
                .enabled(true)
                .build();
        when(userService.getUserById(FOUND_ADMIN)).thenReturn(foundAdminUser);
    }

    @Nested
    @DisplayName("Authority - Member")
    class MemberAuthorityTest {

        @Test
        @WithMockUser(username = MEMBER_USERNAME, authorities = {MEMBER})
        void test_getUserById_statusIsOk_and_usernameIsCorrect_when_userOwnsResource() throws Exception {
            mockMvc.perform(get("/users/{id}", FOUND_MEMBER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(foundMemberUser.getUsername()));
        }

        @Test
        @WithMockUser(username = ADMIN_USERNAME, authorities = {ADMIN})
        void test_getUserById_statusIsOk_and_usernameIsCorrect_when_userIsAdmin() throws Exception {
            mockMvc.perform(get("/users/{id}", FOUND_MEMBER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(foundMemberUser.getUsername()));
        }

        @Test
        @WithMockUser(username = NOSEY_USERNAME, authorities = {MEMBER})
        void test_getUserById_statusIsForbidden_when_userDoesNotOwnResource() throws Exception {
            mockMvc.perform(get("/users/{id}", FOUND_MEMBER))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        void test_getUserById_statusIsUnauthorized_when_userIsAnonymous() throws Exception {
            mockMvc.perform(get("/users/{id}", FOUND_MEMBER))
                    .andExpect(status().isUnauthorized());
        }

    }

}
