package com.aline.usermicroservice;

import com.aline.core.annotations.test.SpringBootUnitTest;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.model.user.UserRole;
import com.aline.usermicroservice.controller.UserController;
import com.aline.usermicroservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import javax.transaction.Transactional;

import static org.mockito.Mockito.when;

@SpringBootUnitTest
@ContextConfiguration
@Sql(scripts = "classpath:scripts/members.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@Slf4j
class ResourceSecurityTest {

    private final static long FOUND_MEMBER = 1;
    private final static long FOUND_ADMIN = 2;
    private final static long NOT_FOUND = 4;

    @Autowired
    UserController controller;

    @MockBean
    UserService userService;

    UserResponse foundMemberUser;
    UserResponse foundAdminUser;

    @BeforeEach
    void setUp() {
        foundMemberUser = UserResponse.builder()
                .id(FOUND_MEMBER)
                .username("member_user")
                .email("member_user@email.com")
                .role(UserRole.MEMBER)
                .firstName("Member")
                .lastName("Boy")
                .enabled(true)
                .build();
        when(userService.getUserById(FOUND_MEMBER)).thenReturn(foundMemberUser);

        foundAdminUser = UserResponse.builder()
                .id(FOUND_ADMIN)
                .username("admin")
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
        @WithMockUser(username = "member_user", authorities = {"member"})
        void test_getUserById_returns_userInfo_when_userInfoRetrieving_is_selfOwned() {
            controller.getUserById(1);
        }

        @Test
        @WithMockUser(username = "admin", authorities = {"administrator"})
        void test_getUserById_returns_userInfo_when_user_is_admin() {
            controller.getUserById(1);
        }

        @Test
        @WithMockUser(username = "not_member_user", authorities = {"member"})
        void test_getUserById_does_not_return_userInfo_when_user_doesNotOwn_the_resource() {
            controller.getUserById(1);
        }

    }

}
