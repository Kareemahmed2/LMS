package com.lms.LearningManagementSystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.LearningManagementSystem.dto.AuthRequest;
import com.lms.LearningManagementSystem.dto.RegisterRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class InputValidationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testInvalidEmailRejection() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("emailuser");
        request.setEmail("not-an-email"); // Invalid email
        request.setPassword("Password1!");
        request.setFirstName("Email");
        request.setLastName("User");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect((ResultMatcher) content().string(containsString("Email should be valid")));
    }

    @Test
    public void testRoleValidation() throws Exception {
        AuthRequest adminRequest = new AuthRequest();
        adminRequest.setUsername("admin");
        adminRequest.setPassword("Admin1234!");

       MvcResult adminResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest))).andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString())
                .get("accessToken").asText();
        mockMvc.perform(put("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("INVALID_ROLE"))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Invalid role specified")));
    }
}
