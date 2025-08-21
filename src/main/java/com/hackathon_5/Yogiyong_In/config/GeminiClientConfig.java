package com.hackathon_5.Yogiyong_In.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiClientConfig {

    @Bean
    public Client geminiClient() {
        String apiKey = System.getenv("GOOGLE_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GOOGLE_API_KEY 환경변수가 설정되지 않았습니다.");
        }

        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

}
