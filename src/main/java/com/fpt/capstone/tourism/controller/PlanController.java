package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.service.GeminiApiService;
import com.fpt.capstone.tourism.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/plans")
public class PlanController {

    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final GeminiApiService geminiApiService;
    private final PlanService planService;


    @GetMapping("/generate")
    public GeneralResponse<String> chat(String prompt) {
        return geminiApiService.getGeminiResponse(prompt);
    }



    @GetMapping("/locations")
    public ResponseEntity<?> locations() {
        return ResponseEntity.ok(planService.getLocations());
    }


    @GetMapping("/locations/all")
    public ResponseEntity<?> locations(@RequestParam(defaultValue = "", required = false) String name) {
        return ResponseEntity.ok(planService.getLocations(name));
    }


}
