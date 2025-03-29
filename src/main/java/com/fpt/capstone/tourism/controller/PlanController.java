package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.service.GeminiApiService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/plan")
public class PlanController {

    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final GeminiApiService geminiApiService;


    @GetMapping("/generate")
    public GeneralResponse<String> chat(String prompt) {
        return geminiApiService.getGeminiResponse(prompt);
    }


}
