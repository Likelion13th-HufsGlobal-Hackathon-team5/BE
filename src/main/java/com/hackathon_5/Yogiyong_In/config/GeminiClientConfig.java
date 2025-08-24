package com.hackathon_5.Yogiyong_In.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;

@Configuration
public class GeminiClientConfig {

    @Value("${google.api-key}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("gemini.api-key가 application.yml에 설정되지 않았습니다.");
        }

        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
