package com.hackathon_5.Yogiyong_In.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiClientConfig {

    @Bean
    public Client geminiClient() {
        // GOOGLE_API_KEY 환경변수 읽어서 Client 생성
        return new Client();
    }
}
