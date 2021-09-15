package com.aline.usermicroservice;

import com.aline.core.annotation.test.SpringBootIntegrationTest;
import com.aline.core.annotation.test.SpringTestProperties;
import com.aline.core.aws.email.EmailService;
import com.aline.core.aws.sms.SMSService;
import com.aline.core.dto.request.AddressChangeRequest;
import com.aline.core.dto.request.AdminUserRegistration;
import com.aline.core.dto.request.ConfirmUserRegistration;
import com.aline.core.dto.request.MemberUserRegistration;
import com.aline.core.dto.request.OtpAuthentication;
import com.aline.core.dto.request.ResetPasswordAuthentication;
import com.aline.core.dto.request.ResetPasswordRequest;
import com.aline.core.dto.request.UserProfileUpdate;
import com.aline.core.dto.request.UserRegistration;
import com.aline.core.dto.response.ContactMethod;
import com.aline.core.dto.response.UserResponse;
import com.aline.core.exception.notfound.UserNotFoundException;
import com.aline.core.model.Member;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.User;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.repository.MemberRepository;
import com.aline.core.repository.UserRegistrationTokenRepository;
import com.aline.core.repository.UserRepository;
import com.aline.core.util.RandomNumberGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;

import static com.aline.core.dto.request.MemberUserRegistration.MemberUserRegistrationBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootIntegrationTest(SpringTestProperties.DISABLE_WEB_SECURITY)
@Slf4j(topic = "Users Integration Test")
@DisplayName("Users Integration Test")
@Sql(scripts = "classpath:scripts/members.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class UserIntegrationTest {

    @MockBean
    EmailService emailService;

    @MockBean
    SMSService smsService;

    @MockBean
    RandomNumberGenerator rng;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    UserRegistrationTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        // Keep emails from sending during integration test. (Don't pull an HBO Max)
        doNothing().when(emailService).sendHtmlEmail(any(), any(), any(), any());
        doNothing().when(smsService).sendSMSMessage(any(), any(), any());
    }

    @Test
    void test_getUserById_status_isOk_when_user_exists() throws Exception {

        // Ensure user exists
        User user = createDefaultMemberUser("look4me");

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andDo(print());

    }

    @Test
    void test_getUserById_status_isNotFound_when_userDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(new UserNotFoundException().getMessage()));
    }

    @Nested
    @DisplayName("User Registration")
    class UserRegistrationTest {
        @Test
        void test_registerUser_status_isCreated_and_location_is_in_header_when_register_memberUser() throws Exception {
            // Create user first.
            createDefaultMemberUser("member");
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
                            .phone("(222) 222-2222")
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

        // For validation
        MemberUserRegistrationBuilder<
                ? extends MemberUserRegistration,
                ? extends MemberUserRegistrationBuilder<?, ?>> cr; // Correct registration

        @Test
        void test_registrationIsValid() throws Exception {
            cr = MemberUserRegistration.builder()
                    .username("member")
                    .password("P@ssword123")
                    .membershipId("12345678")
                    .lastFourOfSSN("2222");
            expectValid(cr.build());
        }

        @Nested
        @DisplayName("Registration is invalid when")
        class RegistrationIsInvalid {

            @BeforeEach
            void setUp() {
                cr = MemberUserRegistration.builder()
                        .username("member")
                        .password("P@ssword123")
                        .membershipId("12345678")
                        .lastFourOfSSN("2222");
            }

            @Test
            void usernameIsNull() throws Exception {
                expectInvalid(cr.username(null).build());
            }

            @Test
            void usernameIsBlank() throws Exception {
                expectInvalid(cr.username("").build());
            }

            @Test
            void usernameIncludesInvalidCharacters() throws Exception {
                expectInvalid(cr.username("username*&").build());
            }

            @Test
            void usernameTooShort() throws Exception {
                // Too short meaning less than 6 characters
                expectInvalid(cr.username("use").build());
            }

            @Test
            void usernameTooLong() throws Exception {
                // More than 20 characters is too long
                expectInvalid(cr.username("abcdefghijklmnopqrstuvwxyz12345").build());
            }

            @Test
            void usernameStartsWithNumber() throws Exception {
                expectInvalid(cr.username("123username").build());
            }

            @Test
            void usernameStartsWithSpecialCharacter() throws Exception {
                expectInvalid(cr.username("__username").build());
            }

            @Test
            void passwordIsNull() throws Exception {
                expectInvalid(cr.password(null).build());
            }

            @Test
            void passwordIsBlank() throws Exception {
                expectInvalid(cr.password("").build());
            }

            @Test
            void passwordContainsNoCapitalLetters() throws Exception {
                expectInvalid(cr.password("p@ssword123").build());
            }

            @Test
            void passwordContainsNoLowercaseLetters() throws Exception {
                expectInvalid(cr.password("P@SSWORD123").build());
            }

            @Test
            void passwordContainsNoSpecialCharacters() throws Exception {
                expectInvalid(cr.password("Password123").build());
            }

            @Test
            void passwordContainsNoNumbers() throws Exception {
                expectInvalid(cr.password("P@ssword").build());
            }

            @Test
            void passwordContainsOnlyNumbers() throws Exception {
                expectInvalid(cr.password("12345678").build());
            }

            @Test
            void passwordIsTooShort() throws Exception {
                expectInvalid(cr.password("P@ss123").build());
            }

        }
    }

    @Nested
    @DisplayName("User Registration Confirmation")
    class UserRegistrationConfirmation {
        @Test
        void test_registerUser_creates_a_userRegistrationToken() throws Exception {
            // Create user first.
            User user = createDefaultMemberUser("testboy");

            assertNotNull(user);

            UserRegistrationToken token = tokenRepository.findByUserId(user.getId()).orElse(null);

            assertNotNull(token);
        }

        @Test
        void test_confirmUserRegistration_enables_user_when_token_is_valid_and_user_exists() throws Exception {
            // Create user first.
            User user = createDefaultMemberUser("testboy");

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
    }

    @Nested
    @DisplayName("Password Reset Test")
    class PasswordResetTest {

        @BeforeEach
        void setUp() {
            when(rng.generateRandomNumberString(6)).thenReturn("123456");
        }

        @Test
        void status_isOk_when_OTP_is_correct() throws Exception {

            createDefaultMemberUser("john_smith");

            ResetPasswordAuthentication authentication = ResetPasswordAuthentication
                    .builder()
                    .username("john_smith")
                    .contactMethod(ContactMethod.PHONE).build();

            String body = mapper.writeValueAsString(authentication);

            // Create new password reset one-time password for the user
            mockMvc.perform(post("/users/password-reset-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk());

            // Check phase
            OtpAuthentication otpAuthentication = OtpAuthentication.builder()
                    .username("john_smith")
                    .otp("123456").build();

            String authBody = mapper.writeValueAsString(otpAuthentication);

            mockMvc.perform(post("/users/otp-authentication")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(authBody))
                    .andExpect(status().isOk());

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .username("john_smith")
                    .otp("123456")
                    .newPassword("NewP@ssword123").build();
            String requestBody = mapper.writeValueAsString(request);

            // Create new password reset request for the user
            mockMvc.perform(put("/users/password-reset")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk());
        }

        @Test
        void status_isForbidden_when_OTP_is_notCorrect_in_checkPhase() throws Exception {

            createDefaultMemberUser("john_smith");

            ResetPasswordAuthentication authentication = ResetPasswordAuthentication
                    .builder()
                    .username("john_smith")
                    .contactMethod(ContactMethod.PHONE).build();

            String body = mapper.writeValueAsString(authentication);

            // Create new password reset one-time password for the user
            mockMvc.perform(post("/users/password-reset-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            OtpAuthentication otpAuthentication = OtpAuthentication.builder()
                    .otp("654321")
                    .username("john_smith").build();
            String requestBody = mapper.writeValueAsString(otpAuthentication);

            // Create new password reset request for the user
            mockMvc.perform(post("/users/otp-authentication")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void status_isForbidden_when_OTP_is_notCorrect() throws Exception {

            createDefaultMemberUser("john_smith");

            ResetPasswordAuthentication authentication = ResetPasswordAuthentication
                    .builder()
                    .username("john_smith")
                    .contactMethod(ContactMethod.PHONE).build();

            String body = mapper.writeValueAsString(authentication);

            // Create new password reset one-time password for the user
            mockMvc.perform(post("/users/password-reset-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .username("john_smith")
                    .otp("654321")
                    .newPassword("NewP@ssword123").build();
            String requestBody = mapper.writeValueAsString(request);

            // Check phase
            OtpAuthentication otpAuthentication = OtpAuthentication.builder()
                    .username("john_smith")
                    .otp("123456").build();

            String authBody = mapper.writeValueAsString(otpAuthentication);

            mockMvc.perform(post("/users/otp-authentication")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authBody))
                    .andExpect(status().isOk());

            // Create new password reset request for the user
            mockMvc.perform(put("/users/password-reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void status_isNotFound_when_user_does_not_exists() throws Exception {

            createDefaultMemberUser("john_smith");

            ResetPasswordAuthentication authentication = ResetPasswordAuthentication
                    .builder()
                    .username("big_boy_smith")
                    .contactMethod(ContactMethod.PHONE).build();

            String body = mapper.writeValueAsString(authentication);

            // Create new password reset one-time password for the user
            mockMvc.perform(post("/users/password-reset-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("User Profile")
    class UserProfileTests {

        @Test
        void test_statusIsOk_when_userExists_and_memberExists() throws Exception {
            // Create default member user
            User user = createDefaultMemberUser("test_boy");
            mockMvc.perform(get("/users/{id}/profile", user.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value("test_boy"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Smith"))
                    .andExpect(jsonPath("$.contactInfo.email").value("johnsmith@email.com"))
                    .andExpect(jsonPath("$.billingAddress.address").value("321 Main St."));
        }

        @Test
        void test_statusIsNotFound_when_userDoesNotExist() throws Exception {
            mockMvc.perform(get("/users/999/profile"))
                    .andExpect(status().isNotFound());
        }

        @Nested
        @DisplayName("Update User Profile")
        class UpdateUserProfileTests {

            @Test
            void test_statusIsNoContent_when_profileExists_and_requestToChangeEmailIsValid() throws Exception {
                MemberUser user = (MemberUser) createDefaultMemberUser("test_boy");
                long id = user.getId();

                UserProfileUpdate updateProfile = UserProfileUpdate.builder()
                        .email("changed@email.com")
                        .build();

                String body = mapper.writeValueAsString(updateProfile);

                mockMvc.perform(put("/users/{id}/profile", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNoContent());

                Member member = memberRepository.findById(user.getMember().getId()).orElse(null);
                assertNotNull(member);
                assertEquals("changed@email.com", member.getApplicant().getEmail());
            }

            @Test
            void test_statusIsNoContent_when_profileExists_and_requestToChangeLastNameIsValid() throws Exception {
                MemberUser user = (MemberUser) createDefaultMemberUser("test_boy");

                UserProfileUpdate updateProfile = UserProfileUpdate.builder()
                        .lastName("Changed")
                        .build();

                String body = mapper.writeValueAsString(updateProfile);

                mockMvc.perform(put("/users/{id}/profile", user.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNoContent());

                Member member = memberRepository.findById(user.getMember().getId()).orElse(null);
                assertNotNull(member);
                assertEquals("Changed", member.getApplicant().getLastName());
            }

            @Test
            void test_statusIsNoContent_when_profileExists_and_requestToChangeUsernameIsValid() throws Exception {
                MemberUser user = (MemberUser) createDefaultMemberUser("test_boy");

                UserProfileUpdate updateProfile = UserProfileUpdate.builder()
                        .username("changed_boy")
                        .build();

                String body = mapper.writeValueAsString(updateProfile);

                mockMvc.perform(put("/users/{id}/profile", user.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNoContent());

                User user1 = userRepository.findByUsername("changed_boy").orElse(null);
                assertNotNull(user1);
                assertEquals("changed_boy", user1.getUsername());
            }

            @Test
            void test_statusIsNoContent_when_profileExists_and_requestToChangeAddressIsValid() throws Exception {
                MemberUser user = (MemberUser) createDefaultMemberUser("test_boy");

                UserProfileUpdate updateProfile = UserProfileUpdate.builder()
                        .address("123 Change St.")
                        .build();

                String body = mapper.writeValueAsString(updateProfile);

                mockMvc.perform(put("/users/{id}/profile", user.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNoContent());

                Member member = memberRepository.findById(user.getMember().getId()).orElse(null);
                assertNotNull(member);
                assertEquals("123 Change St.", member.getApplicant().getAddress());
            }

            @Test
            void test_statusIsNoContent_when_profileExists_and_requestToChangeMailingAddressIsValid() throws Exception {
                MemberUser user = (MemberUser) createDefaultMemberUser("test_boy");

                UserProfileUpdate updateProfile = UserProfileUpdate.builder()
                        .address("PO Box 1234")
                        .build();

                String body = mapper.writeValueAsString(updateProfile);

                mockMvc.perform(put("/users/{id}/profile", user.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNoContent());

                Member member = memberRepository.findById(user.getMember().getId()).orElse(null);
                assertNotNull(member);
                assertEquals("PO Box 1234", member.getApplicant().getMailingAddress());
            }

        }

    }

    /**
     * Create a default user with the first member in
     * the members.sql
     * @return The user grabbed from the MockMVC test results.
     * @throws Exception No exception handling in tests.
     */
    private User createDefaultMemberUser(String username) throws Exception {
        MemberUserRegistration memberUserRegistration =
                MemberUserRegistration.builder()
                        .username(username)
                        .password("P@ssword123")
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

    private void expectValid(UserRegistration userRegistration) throws Exception {
        String body = mapper.writeValueAsString(userRegistration);
        mockMvc.perform(post("/users/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    private void expectInvalid(UserRegistration userRegistration) throws Exception {
        String body = mapper.writeValueAsString(userRegistration);
        mockMvc.perform(post("/users/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

}
