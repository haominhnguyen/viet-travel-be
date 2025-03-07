package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.TokenDTO;
import com.fpt.capstone.tourism.dto.common.UserDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.RegisterRequestDTO;
import com.fpt.capstone.tourism.dto.response.UserInfoResponseDTO;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping()
    public ResponseEntity<GeneralResponse<TokenDTO>> login(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.login(userDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<GeneralResponse<UserInfoResponseDTO>> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.ok(authService.register(registerRequestDTO));
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<GeneralResponse<String>> confirmEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.confirmEmail(token));
    }

    @GetMapping("/roles")
    public ResponseEntity<GeneralResponse<List<Role>>> getUserRoles() {
        return ResponseEntity.ok(authService.getRoles());
    }


}


