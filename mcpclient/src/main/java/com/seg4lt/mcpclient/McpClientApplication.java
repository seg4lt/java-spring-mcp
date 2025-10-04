package com.seg4lt.mcpclient;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class McpClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(McpClientApplication.class, args);
  }
}

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
class ChatController {

  @Qualifier("generalChatClient")
  private final ChatClient generalChatClient;

  @GetMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
  Flux<String> startChat(String userInput) {
    try {
      return this.generalChatClient.prompt()
        // this is where you can add additional tool for this endpoint
        // .toolCallbacks(toolCallbackProvider.getToolCallbacks())
        .user(userInput)
        .stream()
        .content()
        .onErrorReturn(
          "I'm having trouble accessing that information right now."
        );
    } catch (Exception e) {
      System.err.println("Error in stream generation: " + e.getMessage());
      return Flux.just(
        "I'm having trouble accessing that information right now."
      );
    }
  }
}

@RestController
@RequiredArgsConstructor
class McpToolsCheckController {

  private final List<McpSyncClient> mcpClients;

  record ToolInfoDto(String name, String description) {}

  @GetMapping(value = "/mcp-tools", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ToolInfoDto> getTools() {
    List<ToolInfoDto> allTools = new ArrayList<>();

    for (McpSyncClient client : mcpClients) {
      McpSchema.ListToolsResult tools = client.listTools();

      for (McpSchema.Tool tool : tools.tools()) {
        allTools.add(new ToolInfoDto(tool.name(), tool.description()));
      }
    }

    return allTools;
  }
}

@Configuration
class GeneralChatClientConfig {

  @Bean(name = "generalChatClient")
  ChatClient generalChatClient(
    ChatClient.Builder builder,
    LocalTools localTools,
    SyncMcpToolCallbackProvider toolCallbackProvider
  ) {
    return builder
      .defaultSystem(
        """
        You are a helpful AI assistant for a game stats client. You can access game data to answer user questions.

        Guidelines for using tools:
        - Use tools/functions to get accurate data when needed
        - Use tools/functions when user prompt is related to tools/function you can invoke
        - If user prompt is unrelated to tools/functions, answer directly without using tools/functions

        CRITICAL RULES - NEVER VIOLATE THESE:
        1. NEVER output JSON, code blocks, function calls, or any technical structures
        2. NEVER mention tools, functions, callbacks, or technical implementation details
        3. NEVER output anything that starts with { or contains "name": or "parameters"
        4. ALWAYS respond in plain, conversational English only
        5. If data is unavailable, say "I don't have that information right now" - never show technical errors

        Response Requirements:
        - Use only plain text, no formatting, no emojis, no headings
        - Give direct, conversational answers in 1-3 sentences
        - Include specific numbers and names when available
        - If you cannot get the data, give a brief, helpful response

        ABSOLUTELY FORBIDDEN OUTPUTS:
        - Any text containing: {"name": "..", "parameters": {...}}
        - Any JSON-like structures or technical debugging information
        - Any reference to "tools", "callbacks", "functions", or "API calls"

        Bad Examples (NEVER DO THIS):
        - {"name": "tool_name", "parameters": {"limit": 5}}
        - "I'll use the get_server_stats tool to check"
        - Any JSON or code-like output

        Remember: Be conversational and helpful, never technical or revealing implementation details.
        """
      )
      .defaultTools(localTools)
      .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
      .build();
  }
}

@Component
class LocalTools {

  @Tool(
    description = """
    # Local Weather Tool

    ## Use this tool when:
    - User asks about current weather
    - User wants to know weather conditions
    - User asks questions like "what's the weather like?" or "tell me the weather"

    ## Required Params: No parameter needed

      """
  )
  public Map<String, Object> weather() {
    return Map.of(
      "weather",
      "20 F",
      "weatherCelcius",
      "-6.67 C",
      "condition",
      "Sunny"
    );
  }
}
