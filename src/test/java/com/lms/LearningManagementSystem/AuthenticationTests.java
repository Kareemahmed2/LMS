package com.lms.LearningManagementSystem;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import com.lms.LearningManagementSystem.dto.AuthRequest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTests {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testSuccessfulAuthentication() throws Exception {
        // Create authentication request
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("Test1234!");
        
        // Perform login request
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
    
    @Test
    public void testFailedAuthentication() throws Exception {
        // Create authentication request with incorrect password
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");
        
        // Perform login request
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void testProtectedEndpointWithoutAuth() throws Exception {
        // Access protected endpoint without authentication
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void testProtectedEndpointWithAuth() throws Exception {
        // First get a token
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("Test1234!");
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andReturn();
        
        String response = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("accessToken").asText();
        
        // Now access protected endpoint with token
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}