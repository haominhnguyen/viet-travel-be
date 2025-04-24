package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TokenDTO;
import com.fpt.capstone.tourism.dto.common.UserDTO;
import com.fpt.capstone.tourism.service.GroqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/test")
public class TestController {

    private final GroqService groqService;

    @PostMapping("/groq")
    public ResponseEntity<?> test(@RequestBody String prompt) {

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> message1 = new HashMap<>();
        message1.put("role", "user");
        message1.put("content", prompt);
        messages.add(message1);
        String model = "deepseek-r1-distill-llama-70b";
        String response = groqService.callGroqAPI(messages, model);

        return ResponseEntity.ok(GeneralResponse.of(response));
    }

}
