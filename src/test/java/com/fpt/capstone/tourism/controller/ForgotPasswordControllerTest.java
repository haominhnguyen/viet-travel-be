package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.ForgotPasswordRequestDTO;
import com.fpt.capstone.tourism.dto.request.PasswordResetDTO;
import com.fpt.capstone.tourism.service.ForgotPasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class ForgotPasswordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ForgotPasswordService forgotPasswordService;

    @InjectMocks
    private ForgotPasswordController forgotPasswordController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(forgotPasswordController).build();
    }

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordRequestDTO forgotPasswordRequestDTO = new ForgotPasswordRequestDTO();
        forgotPasswordRequestDTO.setEmail("test@example.com");

        when(forgotPasswordService.forgotPassword(anyString()))
                .thenReturn(new GeneralResponse<>(200, "Password reset email sent", "Success"));

        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent"))
                .andExpect(jsonPath("$.data").value("Success"));
    }

    @Test
    void testResetPassword() throws Exception {
        PasswordResetDTO passwordResetDTO = new PasswordResetDTO();
        passwordResetDTO.setPassword("NewPass@123");
        passwordResetDTO.setRePassword("NewPass@123");

        when(forgotPasswordService.resetPassword(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new GeneralResponse<>(200, "Password reset successfully", "Success"));

        mockMvc.perform(put("/reset-password")
                        .param("token", "valid-reset-token")
                        .param("email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"))
                .andExpect(jsonPath("$.data").value("Success"));
    }
}
