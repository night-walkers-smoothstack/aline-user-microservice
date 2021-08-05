package com.aline.usermicroservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
class SmokeTest {

    @Autowired
    UserMicroserviceApplication application;

    @Test
    void contextLoads() {
        assertNotNull(application);
    }

}
