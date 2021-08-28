package com.aline.usermicroservice;

import com.aline.core.annotation.test.SpringBootUnitTest;
import com.aline.core.security.model.JwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootUnitTest
public class JwtTokenTest {

    @Autowired
    SecretKey jwtSecretKey;

    @Test
    void test_fromMethod() {

        final String jwtTokenStr = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhcmpheTA3IiwiYXV0aG9yaXR5IjoibWVtYmVyIiwiaWF0IjoxNjI5OTIwMzYzLCJleHAiOjE2MzExMjk5NjN9.QzpO5xrTFh-hGhtrTbV1NQEadWJfBIFLRYm-rdE_73B-BoQWXSZa0bKBMWarpP00P8tRrE11Cax35IhV8-d2ow";

        final String expectedUsername = "arjay07";

        final Date expectedIat = Date.from(Instant.ofEpochSecond(1629920363));
        final Date expectedExp = Date.from(Instant.ofEpochSecond(1631129963));

        final GrantedAuthority expectedAuthority = new SimpleGrantedAuthority("member");

        final JwtToken token = JwtToken.from(jwtTokenStr, jwtSecretKey);

        assertEquals(expectedUsername, token.getUsername());
        assertEquals(expectedIat, token.getIssuedAt());
        assertEquals(expectedExp, token.getExpiration());

    }

}
