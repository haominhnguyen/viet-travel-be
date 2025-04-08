package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.service.GeminiApiService;
import com.fpt.capstone.tourism.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeminiApiServiceImpl implements GeminiApiService {

    private final ChatClient client;

    public GeminiApiServiceImpl(ChatClient.Builder builder) {
        this.client = builder.build();
    }

    @Override
    public String getGeminiResponse(String prompt) {
        return client.prompt(prompt)
                .call()
                .content();
    }




}
