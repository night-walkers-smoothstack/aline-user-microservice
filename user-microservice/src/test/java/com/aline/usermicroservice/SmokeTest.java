package com.aline.usermicroservice;

import com.aline.core.annotation.test.SpringBootIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootIntegrationTest
class SmokeTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserMicroserviceApplication application;

    @Test
    void contextLoads() {
        assertNotNull(application);
    }

    @Test
    void test_healthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

}
