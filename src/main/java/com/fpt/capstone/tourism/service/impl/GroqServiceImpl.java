package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.service.GroqService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class GroqServiceImpl implements GroqService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final WebClient webClient;

    public GroqServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://eng-today.vercel.app/api/hnam?q=tralalero%20tralala%20la%20gi&sk=tralalerotralala").build();
    }

    @Override
    public String callGroqAPI(String prompt, String model) {
        try {
            Map<String, Object> requestBody = Map.of(
                    //"model", model,
                    "prompt", prompt
            );

            Map response = webClient.post()
                    //.uri("/chat/completions")
                    //.header(HttpHeaders.AUTHORIZATION, "Bearer " + "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")
                    //.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("choices")) {
                return null;
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message != null ? (String) message.get("content") : null;

            if (content != null) {
                // Remove <think>...</think> using regex
                content = Pattern.compile("<think>.*?</think>", Pattern.DOTALL)
                        .matcher(content)
                        .replaceAll("");
            }

            return content;

        } catch (Exception ex) {
            throw BusinessException.of(ex.getMessage(), ex);
        }
    }
}
