package com.lms.LearningManagementSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.LearningManagementSystem.dto.AuthRequest;
import com.lms.LearningManagementSystem.dto.UserUpdateRequest;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationTests {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String adminToken;
    private String instructorToken;
    private String studentToken;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Get admin token
        AuthRequest adminRequest = new AuthRequest();
        adminRequest.setUsername("admin");
        adminRequest.setPassword("Admin1234!");
        
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)))
            .andReturn();
        
        adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString())
            .get("accessToken").asText();
        
        // Get instructor token
        AuthRequest instructorRequest = new AuthRequest();
        instructorRequest.setUsername("instructor");
        instructorRequest.setPassword("Instructor1234!");
        
        MvcResult instructorResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(instructorRequest)))
            .andReturn();
        
        instructorToken = objectMapper.readTree(instructorResult.getResponse().getContentAsString())
            .get("accessToken").asText();
        
        // Get student token
        AuthRequest studentRequest = new AuthRequest();
        studentRequest.setUsername("student");
        studentRequest.setPassword("Student1234!");
        
        MvcResult studentResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequest)))
            .andReturn();
        
        studentToken = objectMapper.readTree(studentResult.getResponse().getContentAsString())
            .get("accessToken").asText();
    }
    
    @Test
    public void testAdminAccessToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }
    
    @Test
    public void testStudentAccessToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isForbidden());
    }
    
    @Test
    public void testInstructorAccessToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + instructorToken))
            .andExpect(status().isForbidden());
    }
    
    @Test
    public void testStudentSelfProfileAccess() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk());
    }
    
    @Test
    public void testUserRoleUpdateAttempt() throws Exception {
        // Create user update request with role change
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setRole("ADMIN");
        
        mockMvc.perform(put("/api/users/student")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Role update not allowed")));
    }
}