package com.seg4lt.mcpclient.config;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfiguration {

  @ConditionalOnProperty(prefix = "spring.ai.model", name = "chat", havingValue = "ollama")
  @Bean
  public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url}") String baseUrl) {
    return OllamaApi.builder()
        .baseUrl(baseUrl)
        .build();
  }
}
