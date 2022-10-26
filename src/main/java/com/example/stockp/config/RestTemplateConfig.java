package com.example.stockp.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @author Mahdi Sharifi
 */
@Configuration
public class RestTemplateConfig {

    @Primary
    @Bean
    public RestTemplate getCustomRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        builder.setConnectTimeout(Duration.ofMillis(2000))
                .setReadTimeout(Duration.ofMillis(5000))
                .build();
        return builder.build();
    }
}
