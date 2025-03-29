package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;

public interface GeminiApiService {
    GeneralResponse<String> getGeminiResponse(String prompt);
}
