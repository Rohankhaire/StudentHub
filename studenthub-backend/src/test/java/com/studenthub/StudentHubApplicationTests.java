package com.studenthub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentHubApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Simple assertion to verify database configuration loads properly
    }

    @Test
    void anonymousUser_whenAccessingAdminEndpoints_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousUser_whenAccessingFacultyEndpoints_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/faculty/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousUser_whenAccessingStudentEndpoints_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/student/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongCredentials_shouldReturnUnauthorized() throws Exception {
        String badCredentialsJson = "{\"email\":\"nonexistent@university.edu\",\"password\":\"wrongpassword\"}";
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badCredentialsJson))
                .andExpect(status().isUnauthorized());
    }
}
