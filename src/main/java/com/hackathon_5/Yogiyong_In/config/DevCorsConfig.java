package com.hackathon_5.Yogiyong_In.config;

import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

@Configuration
@Profile("dev")
public class DevCorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
