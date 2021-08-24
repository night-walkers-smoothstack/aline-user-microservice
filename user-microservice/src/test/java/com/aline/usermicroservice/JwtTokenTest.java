package com.aline.usermicroservice;

import com.aline.core.security.model.JwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
public class JwtTokenTest {

    @Autowired
    SecretKey secretKey;

    @Test
    void test_fromMethod() {

        final String jwtTokenStr = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGhvcml0aWVzIjpbeyJhdXRob3JpdHkiOiJhZG1pbmlzdHJhdG9yIn1dLCJpYXQiOjE2Mjk4Mzc3OTgsImV4cCI6MTYzMDk5ODAwMH0.TAswfzaHh7gx0Yx_qzLB7rZOPe3p7FnknLLghX_HYFzDJXYzzOFHypPpNO7k8eyrKRY9o-I0Ju2bs455rDDLJw";

        final String expectedUsername = "admin";

        final Date expectedIat = Date.from(Instant.ofEpochSecond(1629837798));
        final Date expectedExp = Date.from(Instant.ofEpochSecond(1630998000));

        final GrantedAuthority expectedAuthority = new SimpleGrantedAuthority("administrator");

        final JwtToken token = JwtToken.from(jwtTokenStr, secretKey);

        assertEquals(expectedUsername, token.getUsername());
        assertEquals(expectedIat, token.getIssuedAt());
        assertEquals(expectedExp, token.getExpiration());
        assertEquals(expectedAuthority, token.getAuthority());

    }

}
