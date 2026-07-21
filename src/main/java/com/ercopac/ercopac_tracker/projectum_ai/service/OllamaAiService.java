package com.ercopac.ercopac_tracker.projectum_ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OllamaAiService {

    @Value("${projectum.ai.url}")
    private String ollamaUrl;

    @Value("${projectum.ai.model}")
    private String model;

    private final RestTemplate restTemplate;

    public String ask(String prompt) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("prompt", prompt);
        request.put("stream", false);

        Map response = restTemplate.postForObject(
                ollamaUrl,
                request,
                Map.class
        );

        if (response == null || response.get("response") == null) {
            return "No response received from AI model.";
        }

        return response.get("response").toString();
    }
}