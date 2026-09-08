package com.team202ok.demo.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiAuthenticationTest {
    @Autowired MockMvc mvc;
    @Test void unauthenticatedApiReturns401WithoutLoginRedirect() throws Exception {
        mvc.perform(post("/api/ai/analysis"))
                .andExpect(status().isUnauthorized()).andExpect(header().doesNotExist("Location"));
    }
}
