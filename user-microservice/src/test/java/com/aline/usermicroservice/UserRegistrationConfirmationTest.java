package com.aline.usermicroservice;

import com.aline.core.aws.email.EmailService;
import com.aline.core.model.Applicant;
import com.aline.core.model.Member;
import com.aline.core.model.user.MemberUser;
import com.aline.core.model.user.UserRegistrationToken;
import com.aline.core.repository.UserRegistrationTokenRepository;
import com.aline.usermicroservice.service.UserConfirmationService;
import com.aline.usermicroservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j(topic = "User Registration Confirmation Test")
class UserRegistrationConfirmationTest {

    @Autowired
    UserConfirmationService confirmationService;

    @MockBean
    UserRegistrationTokenRepository repository;

    @MockBean
    UserService userService;

    @MockBean
    EmailService emailService;

    @Test
    void test_calculateExpirationDate_sets_localDateTo_24Hours() {
        UserRegistrationToken token = new UserRegistrationToken();
        LocalDateTime now = LocalDateTime.now(TimeZone.getDefault().toZoneId());
        assertEquals(now.plusHours(24), token.calculateExpirationDate(now));
    }

    @Test
    void test_isExpired_returns_true_when_now_is_after_expiration() {
        UserRegistrationToken token = new UserRegistrationToken();
        token.setCreated(LocalDateTime.now().minusHours(25));
        token.setExpiration(token.calculateExpirationDate(token.getCreated()));
        assertTrue(token.isExpired());
    }

    @Test
    void test_isExpired_returns_false_when_now_is_before_expiration() {
        UserRegistrationToken token = new UserRegistrationToken();
        token.setCreated(LocalDateTime.now().minusHours(23));
        token.setExpiration(token.calculateExpirationDate(token.getCreated()));
        assertFalse(token.isExpired());
    }

    @Test
    void test_isExpired_returns_true_when_expirationDelay_is_set_and_now_is_after_expiration() {
        UserRegistrationToken token = new UserRegistrationToken();
        token.setCreated(LocalDateTime.now().minusSeconds(6));
        token.setExpirationDelay(5);
        token.setExpiration(token.calculateExpirationDate(token.getCreated()));
        assertTrue(token.isExpired());
    }

    @Test
    void test_isExpired_returns_true_when_expirationDelay_is_set_and_now_is_before_expiration() {
        UserRegistrationToken token = new UserRegistrationToken();
        token.setCreated(LocalDateTime.now().minusSeconds(4));
        token.setExpirationDelay(5);
        token.setExpiration(token.calculateExpirationDate(token.getCreated()));
        assertFalse(token.isExpired());
    }

    @Test
    void test_sendMemberUserConfirmationEmail_calls_emailService_sendHtmlEmail() {
        Applicant applicant = Applicant.builder()
                .socialSecurity("123-45-6789")
                .email("applicant@mail.com").build();
        Member member = new Member();
        member.setApplicant(applicant);
        MemberUser user = MemberUser.builder()
                .id(1)
                .username("username")
                .member(member)
                .build();
        UserRegistrationToken token = new UserRegistrationToken();
        token.setToken(UUID.randomUUID());
        token.setUser(user);

        when(repository.save(any())).thenReturn(token);

        confirmationService.sendMemberUserConfirmationEmail(user);

        verify(emailService).sendHtmlEmail(any(), any(), any(), any());
    }

}
