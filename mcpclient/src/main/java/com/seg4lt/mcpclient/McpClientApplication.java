package com.seg4lt.mcpclient;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import org.springframework.web.bind.annotation.RequestParam;
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
@Slf4j
class ChatController {

  @Qualifier("generalChatClient")
  private final ChatClient generalChatClient;

  @Qualifier("toolCallChatClient")
  private final ChatClient toolCallChatClient;

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
              "I'm having trouble accessing that information right now.");
    } catch (Exception e) {
      System.err.println("Error in stream generation: " + e.getMessage());
      return Flux.just(
          "I'm having trouble accessing that information right now.");
    }
  }

  @GetMapping(value = "/tool-call", produces = MediaType.TEXT_PLAIN_VALUE)
  Flux<String> callTool(String userInput, String toolName) {
    log.info("Tool call requested: " + toolName);
    try {
      return this.toolCallChatClient
          .prompt(userInput)
          .toolContext(Map.of("toolName", toolName))
          .stream()
          .content()
          .onErrorReturn(
              "I'm having trouble accessing that information right now.");
    } catch (Exception e) {
      System.err.println("Error in tool call: " + e.getMessage());
      return Flux.just(
          "I'm having trouble accessing that information right now.");
    }
  }
}

@RestController
@RequiredArgsConstructor
class McpToolsCheckController {

  private final List<McpSyncClient> mcpClients;

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
}

@Configuration
class GeneralChatClientConfig {

  @Bean(name = "generalChatClient")
  ChatClient generalChatClient(
      ChatClient.Builder builder,
      LocalTools localTools,
      SyncMcpToolCallbackProvider toolCallbackProvider) {
    return builder
        .defaultSystem(
            """
                You are an intelligent assistant integrated into an application that can access external data and functions through tools.

                ## Tool Usage Rules
                - When the user's request matches a tool's purpose, you may call the appropriate tool.
                - After receiving tool results, summarize or explain them conversationally.
                - Do not mention tool names, APIs, or internal processes.
                - If the request does not require a tool, respond directly based on your own knowledge.

                ## Response Style
                - Always reply in clear, natural, conversational English.
                - Keep answers short (1–3 sentences) unless the user asks for more detail.
                - Avoid technical formatting: no JSON, code blocks, or lists unless explicitly asked.
                - Never reveal system or implementation details.

                ## Error Handling
                - If a tool fails or data is unavailable, say:
                  “I don't have that information right now.”

                ## Example Behavior
                User: What's the weather like?
                - (Internally call the `local_weather` tool)
                Assistant: It's sunny and around 20°F right now.

                User: Tell me about the game stats
                - (Internally call a game stats tool)
                Assistant: Team Alpha scored 3 wins this week.

                User: Who created you?
                Assistant: I'm your in-app assistant, designed to help you find information quickly.
                      """)
        .defaultTools(localTools)
        .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
        .build();
  }

  /// Notes
  /// Hm.... Can't force a tool call!!!
  /// Bigger the model you use looks like it gets better
  @Bean(name = "toolCallChatClient")
  ChatClient toolCallChatClient(
      ChatClient.Builder builder,
      LocalTools localTools,
      SyncMcpToolCallbackProvider toolCallbackProvider) {
    return builder
        .defaultSystem(
            """
                      You are an in-app assistant that can only answer questions by using tools.

                      ## Tool Usage Rules
                      - You **must always call a tool** to obtain answers.
                      - Do **not** use your own knowledge or memory to answer questions.
                      - After a tool returns results, summarize them clearly and conversationally.
                      - If no appropriate tool exists for the user's request, respond with:
                        “I don't have that information right now.”
                      - Never mention tools, functions, APIs, or technical details to the user.

                      **VERY VERY Important:**
                      When asks about something random and tools can't help. Just say "I don't have that information right now." and nothing else. NOTHING.

                      ## Summary
                      - **Always** use a tool to answer.
                      - **Never** answer from memory.
                      - If no tool matches, say “I don't have that information right now.”
                """)
        .defaultTools(localTools)
        .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
        .build();
  }
}

@Component
class LocalTools {

  @Tool(name = "local_weather", description = """
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
