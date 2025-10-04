package com.seg4lt.mcpclient.config;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfiguration {

  @Bean
  public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url}") String baseUrl) {
    return OllamaApi.builder()
        .baseUrl(baseUrl)
        .build();
  }
}
