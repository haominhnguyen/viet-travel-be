package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TokenDTO;
import com.fpt.capstone.tourism.dto.common.UserDTO;
import com.fpt.capstone.tourism.dto.request.RegisterRequestDTO;
import com.fpt.capstone.tourism.dto.response.UserInfoResponseDTO;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testLogin() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("Test@123");

        TokenDTO tokenDTO = TokenDTO.builder()
                .username("testuser")
                .token("fake-jwt-token")
                .expirationTime("24h")
                .build();

        when(authService.login(any(UserDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Login successful", tokenDTO));

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("fake-jwt-token"));
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setUsername("newuser");
        registerRequestDTO.setPassword("NewPass@123");
        registerRequestDTO.setEmail("newuser@example.com");

        UserInfoResponseDTO userInfoResponseDTO = new UserInfoResponseDTO();
        userInfoResponseDTO.setUsername("newuser");
        userInfoResponseDTO.setEmail("newuser@example.com");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Registration successful", userInfoResponseDTO));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    void testConfirmEmail() throws Exception {
        when(authService.confirmEmail("test-token"))
                .thenReturn(new GeneralResponse<>(200, "Email confirmed", "Success"));

        mockMvc.perform(get("/auth/confirm-email")
                        .param("token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email confirmed"))
                .andExpect(jsonPath("$.data").value("Success"));
    }

    @Test
    void testGetUserRoles() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("USER");

        when(authService.getRoles())
                .thenReturn(new GeneralResponse<>(200, "Roles fetched", Collections.singletonList(role)));

        mockMvc.perform(get("/auth/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Roles fetched"))
                .andExpect(jsonPath("$.data[0].roleName").value("USER"));
    }
}
