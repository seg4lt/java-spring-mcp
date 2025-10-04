package com.seg4lt.mcpclient;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

@Component
class LocalTools {

  @Tool(description = """
      # Local Weather Tool

      ## Use this tool when:
      - User asks about current weather
      - User wants to know weather conditions
      - User asks questions like "what's the weather like?" or "tell me the weather"

      ## Required Params: No parameter needed

        """)
  public Map<String, Object> weather() {
    return Map.of(
        "weather",
        "20 F",
        "weatherCelcius",
        "-6.67 C",
        "condition",
        "Sunny");
  }
}

@RestController
@RequestMapping("/api/v1")
class ChatController {

  private final ChatClient generalChatClient;
  private final List<McpSyncClient> mcpClients;
  private final SyncMcpToolCallbackProvider toolCallbackProvider;
  private final LocalTools localTools;

  ChatController(
      ChatClient.Builder chatClientBuilder,
      List<McpSyncClient> mcpClients,
      SyncMcpToolCallbackProvider toolCallbackProvider,
      LocalTools localTools) {
    this.localTools = localTools;
    this.mcpClients = mcpClients;
    this.toolCallbackProvider = toolCallbackProvider;
    this.generalChatClient = chatClientBuilder
        .defaultSystem(
            """
                You are a helpful AI assistant for a game stats client. You can access game data to answer user questions.

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
                """)
        .defaultTools(this.localTools)
        .defaultToolCallbacks(this.toolCallbackProvider.getToolCallbacks())
        .build();
  }

  @GetMapping("/test")
  String test() {
    return "API is working right?";
  }

  record ToolInfoDto(String name, String description) {
  }

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

  @GetMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
  Flux<String> startChat(String userInput) {
    try {
      return this.generalChatClient.prompt()
          // this is where you can add additional tool for this endpoint
          // .toolCallbacks(toolCallbackProvider.getToolCallbacks())
          .user(userInput)
          .stream()
          .content()
          .onErrorReturn("I'm having trouble accessing that information right now.");
    } catch (Exception e) {
      System.err.println("Error in stream generation: " + e.getMessage());
      return Flux.just(
          "I'm having trouble accessing that information right now.");
    }
  }
}
