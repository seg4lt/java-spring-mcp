package com.seg4lt.mcpserver;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
public class McpServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(McpServerApplication.class, args);
  }
}

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
class TestApi {

  @GetMapping("/secret-message")
  @McpTool(description = """
      # Retrieves the super secret message from server.

      ## Use this tool when:
      - User asks about super secret message
      - User wants to know supr secret message
      - User mentions a meaning of secret life
      - User asks questions like "what is the secret message" or "tell me the secret message"

      ## Required Params: No parameter needed
      """, annotations = @McpTool.McpAnnotations(readOnlyHint = true))
  public String test() {
    return "super secret message is `seg4lt` everywhere";
  }
}
