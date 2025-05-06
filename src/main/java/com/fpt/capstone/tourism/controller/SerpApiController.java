package com.fpt.capstone.tourism.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/public/images")
public class SerpApiController {

    @Value("${serpapi.api.key}")
    private String apiKey; // Tải từ application.properties

    private static final String BASE_URL = "https://serpapi.com/search.json";

    private final RestTemplate restTemplate;

    public SerpApiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/get")
    public ResponseEntity<String> getImageSearch(@RequestParam String query) {
        String url = String.format("%s?engine=google_images&q=%s&api_key=%s&num=1", BASE_URL, query, apiKey);
        
        // Gọi SerpAPI và trả kết quả cho frontend
        try {
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error calling SerpAPI");
        }
    }
}