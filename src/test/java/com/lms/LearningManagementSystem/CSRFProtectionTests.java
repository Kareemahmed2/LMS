package com.lms.LearningManagementSystem;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CSRFProtectionTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCsrfProtectionForNonApiEndpoints() throws Exception {
// Test endpoint that should be CSRF protected
        mockMvc.perform(post("/some-web-endpoint"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCsrfIgnoredForAuthEndpoints() throws Exception {
// Auth endpoints should not require CSRF tokens
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
.content("{\"username\":\"test\",\"password\":\"test\"}"))
.andExpect(status().isUnauthorized()); // Should fail with 401, not 403 CSRF
    }
}
