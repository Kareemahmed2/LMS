package com.lms.LearningManagementSystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.LearningManagementSystem.dto.RegisterRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class PasswordSecurityTests {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testPasswordEncoding() {
        String rawPassword = "Test1234!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

// Verify password is encoded (not stored in plain text)
        assertThat(encodedPassword).isNotEqualTo(rawPassword);

// Verify password can be verified
        assertThat(passwordEncoder.matches(rawPassword,encodedPassword)).isTrue();
    }

    @Test
    public void testWeakPasswordRejection() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("weak");
        request.setFirstName("New");
        request.setLastName("User");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Password must be")));  }

    @Test
    public void testStrongPasswordAcceptance() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("stronguser");
        request.setEmail("stronguser@examplecom");
        request.setEmail("stronguser@example.com");
        request.setPassword("StrongP@ss1");
        request.setFirstName("Strong");
        request.setLastName("User");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());}
}